package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.entity.Permission;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.PermissionMapper;
import com.henri_fraise.hff_data_studio.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

	private final PermissionRepository permissionRepository;
	private final PermissionMapper permissionMapper;

	public List<PermissionResponse> getAllPermissions() {
		try {
			return permissionRepository.findAll().stream()
					.map(permissionMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving permissions: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve permissions", ex);
		}
	}

	public List<PermissionResponse> getPermissionsByModule(String module) {
		try {
			return permissionRepository.findByModule(module).stream()
					.map(permissionMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving permissions by module: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve permissions by module", ex);
		}
	}

	public Permission getPermissionEntityById(UUID permissionId) {
		return permissionRepository.findById(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission", permissionId));
	}

	public List<Permission> getPermissionEntitiesByIds(List<UUID> permissionIds) {
		try {
			List<Permission> permissions = permissionRepository.findAllById(permissionIds);
			if (permissions.size() != permissionIds.size()) {
				throw new ResourceNotFoundException("Some permissions not found");
			}
			return permissions;
		} catch (Exception ex) {
			log.error("Error retrieving permission entities: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve permissions", ex);
		}
	}

	public List<Permission> getPermissionsByCategoryId(UUID categoryId) {
		try {
			return permissionRepository.findPermissionsByCategoryId(categoryId);
		} catch (Exception ex) {
			log.error("Error retrieving permissions by category: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve permissions by category", ex);
		}
	}

	public List<String> getAllModules() {
		try {
			return permissionRepository.findAllModules();
		} catch (Exception ex) {
			log.error("Error retrieving modules: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve modules", ex);
		}
	}

	public boolean existsByCode(String code) {
		return permissionRepository.existsByCode(code);
	}
}