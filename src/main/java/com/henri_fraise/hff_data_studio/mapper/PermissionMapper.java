package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {
  public PermissionResponse toResponse(Permission permission) {
    if (permission == null) return null;

    return PermissionResponse.builder()
        .permissionId(permission.getId())
        .code(permission.getCode())
        .label(permission.getLabel())
        .module(permission.getModule())
        .build();
  }
}
