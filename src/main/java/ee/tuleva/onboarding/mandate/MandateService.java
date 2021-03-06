package ee.tuleva.onboarding.mandate;

import com.codeborne.security.mobileid.IdCardSignatureSession;
import com.codeborne.security.mobileid.MobileIdSignatureSession;
import com.codeborne.security.mobileid.SignatureFile;
import ee.tuleva.onboarding.error.response.ErrorsResponse;
import ee.tuleva.onboarding.mandate.command.CreateMandateCommand;
import ee.tuleva.onboarding.mandate.command.CreateMandateCommandToMandateConverter;
import ee.tuleva.onboarding.mandate.exception.InvalidMandateException;
import ee.tuleva.onboarding.mandate.processor.MandateProcessorService;
import ee.tuleva.onboarding.epis.EpisService;
import ee.tuleva.onboarding.mandate.signature.SignatureService;
import ee.tuleva.onboarding.mandate.signature.SmartIdSignatureSession;
import ee.tuleva.onboarding.mandate.statistics.FundTransferStatisticsService;
import ee.tuleva.onboarding.mandate.statistics.FundValueStatistics;
import ee.tuleva.onboarding.mandate.statistics.FundValueStatisticsRepository;
import ee.tuleva.onboarding.notification.email.EmailService;
import ee.tuleva.onboarding.user.User;
import ee.tuleva.onboarding.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MandateService {

    private static final String OUTSTANDING_TRANSACTION = "OUTSTANDING_TRANSACTION";
    private static final String SIGNATURE = "SIGNATURE";

    private final MandateRepository mandateRepository;
    private final SignatureService signService;
    private final CreateMandateCommandToMandateConverter converter;
    private final EmailService emailService;
    private final FundValueStatisticsRepository fundValueStatisticsRepository;
    private final FundTransferStatisticsService fundTransferStatisticsService;
    private final MandateProcessorService mandateProcessor;
    private final MandateFileService mandateFileService;
    private final UserService userService;
    private final EpisService episService;

    public Mandate save(Long userId, CreateMandateCommand createMandateCommand) {
        validateCreateMandateCommand(createMandateCommand);
        Mandate mandate = converter.convert(createMandateCommand);
        mandate.setUser(userService.getById(userId));

        log.info("Saving mandate {}", mandate);
        return mandateRepository.save(mandate);
    }

    private void validateCreateMandateCommand(CreateMandateCommand createMandateCommand) {
        if (countValuesBiggerThanOne(summariseSourceFundTransferAmounts(createMandateCommand)) > 0) {
            throw InvalidMandateException.sourceAmountExceeded();
        }

        if (isSameSourceToTargetTransferPresent(createMandateCommand)) {
            throw InvalidMandateException.sameSourceAndTargetTransferPresent();
        }

    }

    private boolean isSameSourceToTargetTransferPresent(CreateMandateCommand createMandateCommand) {
        return createMandateCommand.getFundTransferExchanges().stream()
                .anyMatch(fte -> fte.getSourceFundIsin().equalsIgnoreCase(fte.getTargetFundIsin()));
    }

    private Map<String, BigDecimal> summariseSourceFundTransferAmounts(CreateMandateCommand createMandateCommand) {
        Map<String, BigDecimal> summaryMap = new HashMap<>();

        createMandateCommand.getFundTransferExchanges().forEach(fte -> {
            if (!summaryMap.containsKey(fte.getSourceFundIsin())) {
                summaryMap.put(fte.getSourceFundIsin(), new BigDecimal(0));
            }

            summaryMap.put(
                    fte.getSourceFundIsin(),
                    summaryMap.get(fte.getSourceFundIsin()).add(fte.getAmount())
            );
        });

        return summaryMap;
    }

    private long countValuesBiggerThanOne(Map<String, BigDecimal> summaryMap) {
        return summaryMap.values().stream().filter(value -> value.compareTo(BigDecimal.ONE) > 0).count();
    }

    public MobileIdSignatureSession mobileIdSign(Long mandateId, Long userId, String phoneNumber) {
        User user = userService.getById(userId);
        List<SignatureFile> files = mandateFileService.getMandateFiles(mandateId, userId);
        return signService.startSign(files, user.getPersonalCode(), phoneNumber);
    }

    public SmartIdSignatureSession smartIdSign(Long mandateId, Long userId) {
        User user = userService.getById(userId);
        List<SignatureFile> files = mandateFileService.getMandateFiles(mandateId, userId);
        return signService.startSmartIdSign(files, user.getPersonalCode());
    }

    public String finalizeSmartIdSignature(Long userId, UUID statisticsIdentifier, Long mandateId, SmartIdSignatureSession session) {
        User user = userService.getById(userId);
        Mandate mandate = mandateRepository.findByIdAndUserId(mandateId, userId);

        if (isMandateSigned(mandate)) {
            return handleSignedMandate(user, mandate, statisticsIdentifier);
        } else {
            return handleUnsignedMandateSmartId(user, mandate, session);
        }
    }

    private String handleUnsignedMandateSmartId(User user, Mandate mandate, SmartIdSignatureSession session) {
        return getStatus(user, mandate, signService.getSignedFile(session));
    }

    private String getStatus(User user, Mandate mandate, byte[] signedFile) {
        if (signedFile != null) {
            persistSignedFile(mandate, signedFile);
            mandateProcessor.start(user, mandate);
            return OUTSTANDING_TRANSACTION; // TODO: use enum
        } else {
            return OUTSTANDING_TRANSACTION; // TODO: use enum
        }
    }


    public IdCardSignatureSession idCardSign(Long mandateId, Long userId, String signingCertificate) {
        List<SignatureFile> files = mandateFileService.getMandateFiles(mandateId, userId);
        return signService.startSign(files, signingCertificate);
    }

    public String finalizeMobileIdSignature(Long userId, UUID statisticsIdentifier, Long mandateId, MobileIdSignatureSession session) {
        User user = userService.getById(userId);
        Mandate mandate = mandateRepository.findByIdAndUserId(mandateId, userId);

        if (isMandateSigned(mandate)) {
            return handleSignedMandate(user, mandate, statisticsIdentifier);
        } else {
            return handleUnsignedMandateMobileId(user, mandate, session);
        }
    }

    private String handleUnsignedMandateMobileId(User user, Mandate mandate, MobileIdSignatureSession session) {
        return getStatus(user, mandate, signService.getSignedFile(session));
    }

    public String finalizeIdCardSignature(Long userId, UUID statisticsIdentifier, Long mandateId, IdCardSignatureSession session, String signedHash) {
        User user = userService.getById(userId);
        Mandate mandate = mandateRepository.findByIdAndUserId(mandateId, userId);

        if (isMandateSigned(mandate)) {
            return handleSignedMandate(user, mandate, statisticsIdentifier);
        } else {
            return handleUnsignedMandateIdCard(user, mandate, session, signedHash);
        }
    }

    private boolean isMandateSigned(Mandate mandate) {
        return mandate.getMandate().isPresent();
    }

    private String handleSignedMandate(User user, Mandate mandate, UUID statisticsIdentifier) {
        if (mandateProcessor.isFinished(mandate)) {
            persistFundTransferExchangeStatistics(statisticsIdentifier, mandate);

            notifyAboutSignedMandate(user,
                    mandate.getId(),
                    mandate.getMandate()
                            .orElseThrow(() -> new RuntimeException("Expecting mandate to be signed, but can not access signed file."))
            );
            episService.clearCache(user);
            handleMandateProcessingErrors(mandate);

            return SIGNATURE; // TODO: use enum
        } else {
            return OUTSTANDING_TRANSACTION; // TODO: use enum
        }
    }

    private void handleMandateProcessingErrors(Mandate mandate) {
        ErrorsResponse errorsResponse = mandateProcessor.getErrors(mandate);

        log.info("Mandate processing errors {}", errorsResponse);
        if (errorsResponse.hasErrors()) {
            throw new InvalidMandateException(errorsResponse);
        }
    }

    private String handleUnsignedMandateIdCard(User user, Mandate mandate, IdCardSignatureSession session, String signedHash) {
        byte[] signedFile = signService.getSignedFile(session, signedHash);
        if (signedFile != null) { // TODO: use Optional
            persistSignedFile(mandate, signedFile);
            mandateProcessor.start(user, mandate);
            return OUTSTANDING_TRANSACTION; // TODO: use enum
        } else {
            throw new IllegalStateException("There is no signed file to persist");
        }
    }

    private void notifyAboutSignedMandate(User user, Long mandateId, byte[] signedFile) {
        emailService.sendMandate(user, mandateId, signedFile);
    }

    private void persistSignedFile(Mandate mandate, byte[] signedFile) {
        mandate.setMandate(signedFile);
        mandateRepository.save(mandate);
    }

    private void persistFundTransferExchangeStatistics(UUID statisticsIdentifier, Mandate mandate) {
        List<FundValueStatistics> fundValueStatisticsList = fundValueStatisticsRepository.findByIdentifier(statisticsIdentifier);
        fundTransferStatisticsService.addFrom(mandate, fundValueStatisticsList);

        // TODO: decide if we need to delete fund value statistics after adding or it might be needed in the same session
        // to generate an other mandate and then be ereased by a chron job
//		fundValueStatisticsList.forEach( fundValueStatistics -> {
//			fundValueStatisticsRepository.delete(fundValueStatistics);
//		});
    }

}
