package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.entity.Permission;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

  public PermissionResponse toResponse(Permission permission) {
    if (permission == null) {
      return null;
    }

    return PermissionResponse.builder()
        .permissionId(permission.getId())
        .code(permission.getCode())
        .label(permission.getLabel())
        .module(permission.getModule())
        .build();
  }

  public List<PermissionResponse> toResponseList(List<Permission> permissions) {
    if (permissions == null) {
      return List.of();
    }
    return permissions.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public Permission toEntity(PermissionResponse response) {
    if (response == null) {
      return null;
    }

    return Permission.builder()
        .id(response.getPermissionId())
        .code(response.getCode())
        .label(response.getLabel())
        .module(response.getModule())
        .build();
  }
}
