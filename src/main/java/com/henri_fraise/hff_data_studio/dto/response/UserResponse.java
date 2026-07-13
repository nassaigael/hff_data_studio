package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String lastName;
    private String firstName;
    private String email;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private UserCategoryResponse category;
}
