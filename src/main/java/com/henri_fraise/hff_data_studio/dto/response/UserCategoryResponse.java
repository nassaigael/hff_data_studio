package com.henri_fraise.hff_data_studio.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCategoryResponse {
    private UUID categoryId;
    private String label;
    private String description;
    private Integer accessLevel;
    private List<PermissionResponse> permissions;
}
