package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.AuditLogResponse;
import com.henri_fraise.hff_data_studio.entity.AuditLog;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.AuditLogMapper;
import com.henri_fraise.hff_data_studio.repository.AuditLogRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;
	private final AuditLogMapper auditLogMapper;
	private final SecurityUtils securityUtils;
	private final UserService userService;

	public AuditLogService(AuditLogRepository auditLogRepository,
	                       AuditLogMapper auditLogMapper,
	                       SecurityUtils securityUtils,
	                       @Lazy UserService userService) {
		this.auditLogRepository = auditLogRepository;
		this.auditLogMapper = auditLogMapper;
		this.securityUtils = securityUtils;
		this.userService = userService;
	}

	@Transactional
	public void logAction(String action, String entity, UUID entityId, String details) {
		try {
			User user = getCurrentUser();
			AuditLog auditLog = AuditLog.builder()
					.action(action)
					.concernedEntity(entity)
					.entityId(entityId)
					.details(details)
					.user(user)
					.ipAddress(securityUtils.getClientIpAddress())
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

	@Transactional
	public void logActionWithUserAndIp(String action, String entity, UUID entityId,
	                                   String details, UUID userId, String ipAddress) {
		try {
			User user = userService.getUserEntityById(userId);
			AuditLog auditLog = AuditLog.builder()
					.action(action)
					.concernedEntity(entity)
					.entityId(entityId)
					.details(details)
					.user(user)
					.ipAddress(ipAddress)
					.build();
			auditLogRepository.save(auditLog);
		} catch (Exception ex) {
			log.error("Error logging audit action with user and ip: {}", ex.getMessage(), ex);
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

	public Page<AuditLogResponse> getAuditLogs(Pageable pageable, UUID userId,
	                                           LocalDateTime startDate, LocalDateTime endDate, String action) {
		try {
			Page<AuditLog> logs;
			if (userId != null && startDate != null && endDate != null && action != null) {
				logs = auditLogRepository.findByUserIdAndActionAndActionDateBetween(userId, action, startDate, endDate, pageable);
			} else if (userId != null && startDate != null && endDate != null) {
				logs = auditLogRepository.findByUserIdAndActionDateBetween(userId, startDate, endDate, pageable);
			} else if (userId != null && action != null) {
				logs = auditLogRepository.findByUserIdAndAction(userId, action, pageable);
			} else if (userId != null) {
				logs = auditLogRepository.findByUserIdOrderByActionDateDesc(userId, pageable);
			} else if (action != null && startDate != null && endDate != null) {
				logs = auditLogRepository.findByActionAndActionDateBetween(action, startDate, endDate, pageable);
			} else if (action != null) {
				logs = auditLogRepository.findByAction(action, pageable);
			} else if (startDate != null && endDate != null) {
				logs = auditLogRepository.findByActionDateBetween(startDate, endDate, pageable);
			} else {
				logs = auditLogRepository.findAll(pageable);
			}
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving audit logs with filters: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit logs with filters", ex);
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

	public Page<AuditLogResponse> getAuditLogsByAction(String action, Pageable pageable) {
		try {
			Page<AuditLog> logs = auditLogRepository.findByAction(action, pageable);
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving audit logs by action: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit logs by action", ex);
		}
	}

	public Page<AuditLogResponse> getAuditLogsByEntity(String entity, UUID entityId, Pageable pageable) {
		try {
			Page<AuditLog> logs = auditLogRepository.findByConcernedEntityAndEntityId(entity, entityId, pageable);
			return logs.map(auditLogMapper::toResponse);
		} catch (Exception ex) {
			log.error("Error retrieving audit logs by entity: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit logs by entity", ex);
		}
	}

	public AuditLogResponse getAuditLogById(UUID logId) {
		try {
			AuditLog log = auditLogRepository.findById(logId)
					.orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + logId));
			return auditLogMapper.toResponse(log);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error retrieving audit log: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve audit log", ex);
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

	public List<String> getDistinctActions() {
		try {
			return auditLogRepository.findDistinctActions();
		} catch (Exception ex) {
			log.error("Error getting distinct audit actions: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get distinct audit actions", ex);
		}
	}

	public List<String> getDistinctEntities() {
		try {
			return auditLogRepository.findDistinctEntities();
		} catch (Exception ex) {
			log.error("Error getting distinct audit entities: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get distinct audit entities", ex);
		}
	}

	public Map<String, Long> getActionCounts() {
		try {
			Map<String, Long> counts = new HashMap<>();
			List<Object[]> results = auditLogRepository.countGroupByAction();
			for (Object[] result : results) {
				counts.put((String) result[0], (Long) result[1]);
			}
			return counts;
		} catch (Exception ex) {
			log.error("Error getting audit action counts: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get audit action counts", ex);
		}
	}

	public Map<String, Long> getEntityCounts() {
		try {
			Map<String, Long> counts = new HashMap<>();
			List<Object[]> results = auditLogRepository.countGroupByEntity();
			for (Object[] result : results) {
				counts.put((String) result[0], (Long) result[1]);
			}
			return counts;
		} catch (Exception ex) {
			log.error("Error getting audit entity counts: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get audit entity counts", ex);
		}
	}

	public Map<String, Object> getAuditSummary() {
		try {
			Map<String, Object> summary = new HashMap<>();
			summary.put("totalLogs", auditLogRepository.count());
			summary.put("actionCounts", getActionCounts());
			summary.put("entityCounts", getEntityCounts());
			summary.put("distinctActions", getDistinctActions());
			summary.put("distinctEntities", getDistinctEntities());
			summary.put("recentLogs", auditLogRepository.findRecentLogs(Pageable.ofSize(10)));
			return summary;
		} catch (Exception ex) {
			log.error("Error getting audit summary: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get audit summary", ex);
		}
	}

	@Transactional
	public void deleteAuditLogsOlderThan(LocalDateTime date) {
		try {
			long deleted = auditLogRepository.deleteByActionDateBefore(date);
			log.info("Deleted {} audit logs older than {}", deleted, date);
		} catch (Exception ex) {
			log.error("Error deleting old audit logs: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete old audit logs", ex);
		}
	}

	@Transactional
	public void deleteAuditLogsByUser(UUID userId) {
		try {
			auditLogRepository.deleteByUserId(userId);
			log.info("Deleted audit logs for user: {}", userId);
		} catch (Exception ex) {
			log.error("Error deleting audit logs by user: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete audit logs by user", ex);
		}
	}

	public long countAuditLogs() {
		return auditLogRepository.count();
	}

	public long countAuditLogsByAction(String action) {
		return auditLogRepository.countByAction(action);
	}

	public long countAuditLogsByUser(UUID userId) {
		return auditLogRepository.countByUserId(userId);
	}

	public long countAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		return auditLogRepository.countByActionDateBetween(startDate, endDate);
	}

	public List<String> getActions() {
		return getDistinctActions();
	}

	private User getCurrentUser() {
		try {
			return userService.getUserEntityById(securityUtils.getCurrentUserId());
		} catch (Exception e) {
			return null;
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
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}
		return ip;
	}
}