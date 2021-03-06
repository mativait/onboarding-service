package ee.tuleva.onboarding.fund;

import ee.tuleva.onboarding.fund.response.FundResponse;
import ee.tuleva.onboarding.fund.statistics.PensionFundStatistics;
import ee.tuleva.onboarding.fund.statistics.PensionFundStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FundService {

  private final FundRepository fundRepository;
  private final PensionFundStatisticsService pensionFundStatisticsService;

  public List<FundResponse> getFunds(Optional<String> fundManagerName) {
     return stream(fundsBy(fundManagerName).spliterator(), false)
       .map(fund -> new FundResponse(fund, statistics(fund)))
       .collect(toList());
  }

  private PensionFundStatistics statistics(Fund fund) {
    return pensionFundStatisticsService.getCachedStatistics().stream()
      .filter(statistic -> Objects.equals(statistic.getIsin(), fund.getIsin()))
      .findFirst()
      .orElse(PensionFundStatistics.NULL);
  }

  private Iterable<Fund> fundsBy(Optional<String> fundManagerName) {
    return fundManagerName
      .map(fundRepository::findByFundManagerNameIgnoreCase)
      .orElse(fundRepository.findAll());
  }

}
