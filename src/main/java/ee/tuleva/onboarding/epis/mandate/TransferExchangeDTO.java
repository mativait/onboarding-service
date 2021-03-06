package ee.tuleva.onboarding.epis.mandate;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TransferExchangeDTO implements Serializable {

    private String currency;
    private Instant date;
    private String id;
    private String documentNumber;
    private BigDecimal amount;
    private MandateApplicationStatus status;
    private String sourceFundIsin;
    private String targetFundIsin;

}
