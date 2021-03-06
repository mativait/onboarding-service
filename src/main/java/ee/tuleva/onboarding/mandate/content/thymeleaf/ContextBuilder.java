package ee.tuleva.onboarding.mandate.content.thymeleaf;

import ee.tuleva.onboarding.fund.Fund;
import ee.tuleva.onboarding.mandate.FundTransferExchange;
import ee.tuleva.onboarding.mandate.Mandate;
import ee.tuleva.onboarding.user.User;
import ee.tuleva.onboarding.epis.contact.UserPreferences;
import org.thymeleaf.context.Context;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContextBuilder {

    private Context ctx = new Context();

    public Context build() {
        return ctx;
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public ContextBuilder user(User user) {
        ctx.setVariable("email", user.getEmail());
        ctx.setVariable("firstName", user.getFirstName());
        ctx.setVariable("lastName", user.getLastName());
        ctx.setVariable("idCode", user.getPersonalCode());
        ctx.setVariable("phoneNumber", user.getPhoneNumber());
        return this;
    }

    public ContextBuilder mandate(Mandate mandate) {
        DateTimeFormatter formatterEst = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
        String documentDate = formatterEst.format(mandate.getCreatedDate());

        DateTimeFormatter formatterEst2 = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
        String documentDatePPKKAAAA = formatterEst2.format(mandate.getCreatedDate());

        ctx.setVariable("documentDate", documentDate);
        ctx.setVariable("documentDatePPKKAAAA", documentDatePPKKAAAA);

        return this;
    }

    public ContextBuilder funds(List<Fund> funds) {
        //sort because by law, funds need to be in alphabetical order
        funds.sort((Fund fund1, Fund fund2) -> fund1.getName().compareToIgnoreCase(fund2.getName()));
        ctx.setVariable("funds", funds);
        ctx.setVariable(
                "fundIsinNames",
                funds.stream().collect(Collectors.toMap(Fund::getIsin, Fund::getName))
        );
        return this;
    }

    public ContextBuilder transactionId(String transactionId) {
        ctx.setVariable("transactionId", transactionId);
        return this;
    }

    public ContextBuilder futureContributionFundIsin(String futureContributionFundIsin) {
        ctx.setVariable("selectedFundIsin", futureContributionFundIsin);
        return this;
    }

    public ContextBuilder documentNumber(String documentNumber) {
        ctx.setVariable("documentNumber", documentNumber);
        return this;
    }

    public ContextBuilder fundTransferExchanges(List<FundTransferExchange> fundTransferExchanges) {
        ctx.setVariable("fundTransferExchanges", fundTransferExchanges);
        return this;
    }

    public ContextBuilder groupedTransferExchanges(List<FundTransferExchange> fundTransferExchanges) {

        Map<String, List<FundTransferExchange>> groupedTransferExchanges
                = fundTransferExchanges.stream()
                .collect(Collectors.groupingBy(FundTransferExchange::getSourceFundIsin));

        ctx.setVariable("groupedFundTransferExchanges", groupedTransferExchanges);
        return this;
    }

    public ContextBuilder userPreferences(UserPreferences userPreferences) {
        ctx.setVariable("userPreferences", userPreferences);

        ctx.setVariable("addressLine1", userPreferences.getAddressRow1());
        ctx.setVariable("addressLine2", userPreferences.getAddressRow2());
        ctx.setVariable("settlement", userPreferences.getAddressRow2());
        ctx.setVariable("countryCode", userPreferences.getCountry());
        ctx.setVariable("postCode", userPreferences.getPostalIndex());
        ctx.setVariable("districtCode", userPreferences.getDistrictCode());
        if (ctx.getVariables().get("email") == null) {
            ctx.setVariable("email", userPreferences.getEmail());
        }
        return this;
    }

}
