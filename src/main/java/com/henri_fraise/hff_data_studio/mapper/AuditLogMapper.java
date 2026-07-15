package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.AuditLogResponse;
import com.henri_fraise.hff_data_studio.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

  public AuditLogResponse toResponse(AuditLog log) {
    if (log == null) return null;

    return AuditLogResponse.builder()
        .logId(log.getId())
        .action(log.getAction())
        .concernedEntity(log.getConcernedEntity())
        .entityId(log.getEntityId())
        .actionDate(log.getActionDate())
        .ipAddress(log.getIpAddress())
        .userId(log.getUser() != null ? log.getUser().getId() : null)
        .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
        .userFullName(
            log.getUser() != null
                ? log.getUser().getFirstName() + " " + log.getUser().getLastName()
                : null)
        .build();
  }
}
