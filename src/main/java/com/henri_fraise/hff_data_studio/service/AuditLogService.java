package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.AuditLogResponse;
import com.henri_fraise.hff_data_studio.entity.AuditLog;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.mapper.AuditLogMapper;
import com.henri_fraise.hff_data_studio.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;
	private final AuditLogMapper auditLogMapper;
	private final UserService userService;

	@Transactional
	public void logAction(String action, String entity, UUID entityId, String details) {
		try {
			AuditLog auditLog = AuditLog.builder()
					.action(action)
					.concernedEntity(entity)
					.entityId(entityId)
					.details(details)
					.build();
			auditLogRepository.save(auditLog);
		} catch (Exception ex) {
			log.error("Error logging audit action: {}", ex.getMessage(), ex);
		}
	}

	@Transactional
	public void logActionWithUser(String action, String entity, UUID entityId,
	                              String details, UUID userId, HttpServletRequest request) {
		try {
			User user = userService.getUserEntityById(userId);

			AuditLog auditLog = AuditLog.builder()
					.action(action)
					.concernedEntity(entity)
					.entityId(entityId)
					.details(details)
					.user(user)
					.ipAddress(getClientIp(request))
					.build();
			auditLogRepository.save(auditLog);
		} catch (Exception ex) {
			log.error("Error logging audit action with user: {}", ex.getMessage(), ex);
		}
	}

	public Page<AuditLogResponse> getAuditLogs(Pageable pageable) {
		try {
			Page<AuditLog> logs = auditLogRepository.findAll(pageable);
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving audit logs: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit logs", ex);
		}
	}

	public Page<AuditLogResponse> getAuditLogsByUser(UUID userId, Pageable pageable) {
		try {
			Page<AuditLog> logs = auditLogRepository.findByUserIdOrderByActionDateDesc(userId, pageable);
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving audit logs by user: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit logs by user", ex);
		}
	}

	public Page<AuditLogResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
		try {
			Page<AuditLog> logs = auditLogRepository.findByActionDateBetween(startDate, endDate, pageable);
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving audit logs by date range: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit logs by date range", ex);
		}
	}

	public Page<AuditLogResponse> searchAuditLogs(String searchTerm, Pageable pageable) {
		try {
			Page<AuditLog> logs = auditLogRepository.findByActionContainingIgnoreCase(searchTerm, pageable);
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error searching audit logs: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search audit logs", ex);
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}