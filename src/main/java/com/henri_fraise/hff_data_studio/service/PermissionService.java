package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.Permission;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

	private final PermissionRepository permissionRepository;

	public Permission getPermissionEntityById(UUID permissionId) {
		return permissionRepository.findById(permissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));
	}

	public List<Permission> getAllPermissions() {
		return permissionRepository.findAll();
	}

	public List<Permission> getPermissionsByModule(String module) {
		return permissionRepository.findByModule(module);
	}

	public List<Permission> getPermissionsByModules(List<String> modules) {
		return permissionRepository.findByModuleIn(modules);
	}

	public List<Permission> getActivePermissions() {
		return permissionRepository.findByIsActiveTrue();
	}

	public List<Permission> getPermissionsByCategoryId(UUID categoryId) {
		return permissionRepository.findPermissionsByCategoryId(categoryId);
	}

	public List<String> getPermissionCodesByCategoryId(UUID categoryId) {
		return permissionRepository.findPermissionCodesByCategoryId(categoryId);
	}

	public boolean existsByCode(String code) {
		return permissionRepository.existsByCode(code);
	}

	public long countPermissionsByCategory(UUID categoryId) {
		return permissionRepository.countPermissionsByCategoryId(categoryId);
	}

	public List<Permission> getPermissionEntitiesByIds(List<UUID> permissionIds) {
		return permissionRepository.findAllById(permissionIds);
	}
}