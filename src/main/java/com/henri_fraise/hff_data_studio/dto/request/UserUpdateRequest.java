package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.UserRole;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

  private String lastName;

  private String firstName;

  @Email(message = "Email must be valid")
  private String email;

  private Boolean isActive;

  private UserRole role;

  private UUID categoryId;

  private String newPassword;
}