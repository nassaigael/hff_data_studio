package com.henri_fraise.hff_data_studio.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionResponse {
    private UUID permissionId;
    private String code;
    private String label;
    private String module;
}
