package com.henri_fraise.hff_data_studio.dto.request;

import java.util.UUID;
import javax.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

  private String lastName;

  private String firstName;

  @Email(message = "Email must be valid")
  private String email;

  private Boolean isActive;

  private UUID categoryId;

  private String newPassword;
}
