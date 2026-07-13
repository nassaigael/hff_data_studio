package com.henri_fraise.hff_data_studio.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionUpdateRequest {

    private List<UUID> permissionIDs;
}
