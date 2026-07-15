package com.henri_fraise.hff_data_studio.dto.response;

import java.util.UUID;
import lombok.*;

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
