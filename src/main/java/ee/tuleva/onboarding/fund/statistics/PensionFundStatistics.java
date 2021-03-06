package ee.tuleva.onboarding.fund.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PensionFundStatistics implements Serializable {

  private static final long serialVersionUID = 6065879655793615150L;

  public static final PensionFundStatistics NULL = new PensionFundStatistics();

  @XmlAttribute(name = "ISIN")
  private String isin;

  @XmlAttribute(name = "VOLUME")
  private BigDecimal volume;

  @XmlAttribute(name = "NAV")
  private BigDecimal nav;

}
