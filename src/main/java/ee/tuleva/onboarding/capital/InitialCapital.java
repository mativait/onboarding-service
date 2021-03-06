package ee.tuleva.onboarding.capital;

import ee.tuleva.onboarding.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Builder
@Entity
@Table(name = "initial_capital")
@AllArgsConstructor
@NoArgsConstructor
public class InitialCapital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private BigDecimal ownershipFraction;

    @OneToOne
    private User user;

}
