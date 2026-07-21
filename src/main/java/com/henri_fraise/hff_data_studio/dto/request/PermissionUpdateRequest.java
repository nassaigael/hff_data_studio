package com.henri_fraise.hff_data_studio.dto.request;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionUpdateRequest {

  private List<UUID> permissionIds;
}
