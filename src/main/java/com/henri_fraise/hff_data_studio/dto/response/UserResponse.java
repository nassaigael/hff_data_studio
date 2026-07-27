package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private UUID userId;

  private String lastName;

  private String firstName;

  private String email;

  private Boolean isActive;

  private UserRole role;

  private String roleLabel;

  private String roleDescription;

  private LocalDateTime createdAt;

  private LocalDateTime lastLogin;

  private UserCategoryResponse category;
}