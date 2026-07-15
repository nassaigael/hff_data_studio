package com.henri_fraise.hff_data_studio.dto.request;

import java.util.UUID;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationRequest {

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password is required")
  private String newPassword;

  @NotBlank(message = "Category is required")
  private UUID categoryId;

  private Boolean isActive;
}
