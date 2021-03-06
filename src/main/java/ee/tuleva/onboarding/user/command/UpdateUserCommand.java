package ee.tuleva.onboarding.user.command;

import lombok.Data;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

@Data
public class UpdateUserCommand {

  @NotNull
  @Email
  private String email;

  private String phoneNumber;

}
