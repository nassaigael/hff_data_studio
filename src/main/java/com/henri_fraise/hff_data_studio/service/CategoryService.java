package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.CategoryCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.PermissionResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.entity.Permission;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.PermissionMapper;
import com.henri_fraise.hff_data_studio.mapper.UserCategoryMapper;
import com.henri_fraise.hff_data_studio.repository.UserCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

	private final UserCategoryRepository categoryRepository;
	private final UserCategoryMapper categoryMapper;
	private final PermissionMapper permissionMapper;
	private final PermissionService permissionService;
	private final AuditLogService auditLogService;

	// ==================== GET / FIND METHODS ====================

	public UserCategory getCategoryEntityById(UUID categoryId) {
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
	}

	public UserCategory getCategoryEntityByLabel(String label) {
		return categoryRepository.findByLabel(label)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with label: " + label));
	}

	public UserCategoryResponse getCategoryById(UUID categoryId) {
		UserCategory category = getCategoryEntityById(categoryId);
		return categoryMapper.toResponse(category);
	}

	public UserCategoryResponse getCategoryByLabel(String label) {
		UserCategory category = getCategoryEntityByLabel(label);
		return categoryMapper.toResponse(category);
	}

	public List<UserCategoryResponse> getAllCategories() {
		try {
			List<UserCategory> categories = categoryRepository.findAll();
			return categories.stream()
					.map(categoryMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving categories: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve categories", ex);
		}
	}

	public List<UserCategoryResponse> getCategoriesWithUsers() {
		try {
			List<UserCategory> categories = categoryRepository.findCategoriesWithUsers();
			return categories.stream()
					.map(categoryMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving categories with users: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve categories with users", ex);
		}
	}

	public List<UserCategoryResponse> getCategoriesByAccessLevel(int accessLevel) {
		try {
			List<UserCategory> categories = categoryRepository.findByAccessLevelGreaterThanEqual(accessLevel);
			return categories.stream()
					.map(categoryMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving categories by access level: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve categories by access level", ex);
		}
	}

	// ==================== CREATE METHODS ====================

	@Transactional
	public UserCategoryResponse createCategory(CategoryCreationRequest request) {
		if (categoryRepository.existsByLabel(request.getLabel())) {
			throw new ResourceAlreadyExistsException("Category already exists with label: " + request.getLabel());
		}

		try {
			UserCategory category = UserCategory.builder()
					.label(request.getLabel().toUpperCase())
					.description(request.getDescription())
					.accessLevel(request.getAccessLevel())
					.build();

			UserCategory saved = categoryRepository.save(category);
			log.info("Category created successfully: {} ({})", saved.getLabel(), saved.getId());

			auditLogService.logAction(
					"CATEGORY_CREATED",
					"UserCategory",
					saved.getId(),
					"Category " + saved.getLabel() + " created"
			);

			return categoryMapper.toResponse(saved);
		} catch (Exception ex) {
			log.error("Error creating category: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create category", ex);
		}
	}

	@Transactional
	public UserCategory createCategoryEntity(UserCategory category) {
		try {
			if (categoryRepository.existsByLabel(category.getLabel())) {
				throw new ResourceAlreadyExistsException("Category already exists with label: " + category.getLabel());
			}
			UserCategory saved = categoryRepository.save(category);
			log.info("Category entity created: {}", saved.getLabel());
			return saved;
		} catch (Exception ex) {
			log.error("Error creating category entity: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to create category entity", ex);
		}
	}

	// ==================== UPDATE METHODS ====================

	@Transactional
	public UserCategoryResponse updateCategory(UUID categoryId, CategoryCreationRequest request) {
		UserCategory category = getCategoryEntityById(categoryId);

		if (!category.getLabel().equals(request.getLabel().toUpperCase()) &&
				categoryRepository.existsByLabel(request.getLabel())) {
			throw new ResourceAlreadyExistsException("Category already exists with label: " + request.getLabel());
		}

		try {
			category.setLabel(request.getLabel().toUpperCase());
			category.setDescription(request.getDescription());
			category.setAccessLevel(request.getAccessLevel());

			UserCategory updated = categoryRepository.save(category);
			log.info("Category updated successfully: {} ({})", updated.getLabel(), updated.getId());

			auditLogService.logAction(
					"CATEGORY_UPDATED",
					"UserCategory",
					updated.getId(),
					"Category " + updated.getLabel() + " updated"
			);

			return categoryMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating category: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update category", ex);
		}
	}

	@Transactional
	public UserCategory updateCategoryEntity(UserCategory category) {
		try {
			UserCategory updated = categoryRepository.save(category);
			log.info("Category entity updated: {}", updated.getLabel());
			return updated;
		} catch (Exception ex) {
			log.error("Error updating category entity: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update category entity", ex);
		}
	}

	// ==================== DELETE METHODS ====================

	@Transactional
	public void deleteCategory(UUID categoryId) {
		UserCategory category = getCategoryEntityById(categoryId);

		try {
			long userCount = categoryRepository.countUsersByCategoryId(categoryId);
			if (userCount > 0) {
				throw new IllegalStateException("Cannot delete category with " + userCount + " users assigned");
			}

			categoryRepository.delete(category);
			log.info("Category deleted successfully: {} ({})", category.getLabel(), categoryId);

			auditLogService.logAction(
					"CATEGORY_DELETED",
					"UserCategory",
					categoryId,
					"Category " + category.getLabel() + " deleted"
			);
		} catch (IllegalStateException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error deleting category: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete category", ex);
		}
	}

	@Transactional
	public void deleteCategoryByLabel(String label) {
		UserCategory category = getCategoryEntityByLabel(label);
		deleteCategory(category.getId());
	}

	// ==================== PERMISSION METHODS ====================

	@Transactional
	public void updateCategoryPermissions(UUID categoryId, List<UUID> permissionIds) {
		UserCategory category = getCategoryEntityById(categoryId);

		try {
			List<Permission> permissions = permissionIds.stream()
					.map(permissionService::getPermissionEntityById)
					.collect(Collectors.toList());

			category.setPermissions(permissions);
			categoryRepository.save(category);

			log.info("Permissions updated for category: {} ({} permissions)", category.getLabel(), permissions.size());

			auditLogService.logAction(
					"CATEGORY_PERMISSIONS_UPDATED",
					"UserCategory",
					categoryId,
					"Permissions updated for category " + category.getLabel() + " - " + permissions.size() + " permissions"
			);
		} catch (Exception ex) {
			log.error("Error updating category permissions: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update category permissions", ex);
		}
	}

	public List<PermissionResponse> getCategoryPermissions(UUID categoryId) {
		try {
			UserCategory category = getCategoryEntityById(categoryId);
			return category.getPermissions().stream()
					.map(permissionMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving category permissions: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve category permissions", ex);
		}
	}

	public List<PermissionResponse> getAllPermissions() {
		try {
			return new ArrayList<>(permissionService.getAllPermissions());
		} catch (Exception ex) {
			log.error("Error retrieving permissions: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve permissions", ex);
		}
	}

	@Transactional
	public void addPermissionToCategory(UUID categoryId, UUID permissionId) {
		UserCategory category = getCategoryEntityById(categoryId);
		Permission permission = permissionService.getPermissionEntityById(permissionId);

		try {
			List<Permission> permissions = category.getPermissions();
			if (!permissions.contains(permission)) {
				permissions.add(permission);
				category.setPermissions(permissions);
				categoryRepository.save(category);
				log.info("Permission {} added to category {}", permission.getCode(), category.getLabel());
			}
		} catch (Exception ex) {
			log.error("Error adding permission to category: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to add permission to category", ex);
		}
	}

	@Transactional
	public void removePermissionFromCategory(UUID categoryId, UUID permissionId) {
		UserCategory category = getCategoryEntityById(categoryId);
		Permission permission = permissionService.getPermissionEntityById(permissionId);

		try {
			List<Permission> permissions = category.getPermissions();
			permissions.remove(permission);
			category.setPermissions(permissions);
			categoryRepository.save(category);
			log.info("Permission {} removed from category {}", permission.getCode(), category.getLabel());
		} catch (Exception ex) {
			log.error("Error removing permission from category: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to remove permission from category", ex);
		}
	}

	// ==================== COUNT METHODS ====================

	public long countCategories() {
		return categoryRepository.count();
	}

	public long countUsersByCategory(UUID categoryId) {
		return categoryRepository.countUsersByCategoryId(categoryId);
	}

	public long countUsersByCategoryLabel(String label) {
		return categoryRepository.countUsersByCategoryLabel(label);
	}

	public boolean existsByLabel(String label) {
		return categoryRepository.existsByLabel(label);
	}

	public boolean existsById(UUID categoryId) {
		return categoryRepository.existsById(categoryId);
	}

	// ==================== STATISTICS METHODS ====================

	public List<Object[]> getCategoryStatistics() {
		try {
			return categoryRepository.getCategoryStatistics();
		} catch (Exception ex) {
			log.error("Error retrieving category statistics: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve category statistics", ex);
		}
	}

	public UserCategory getDefaultCategory() {
		return categoryRepository.findByLabel("INVITE")
				.orElseGet(() -> categoryRepository.findFirstByOrderByAccessLevelAsc()
						.orElse(null));
	}

	public UserCategory getAdminCategory() {
		return categoryRepository.findByLabel("ADMIN")
				.orElseThrow(() -> new ResourceNotFoundException("Admin category not found"));
	}

	public UserCategory getAnalystCategory() {
		return categoryRepository.findByLabel("DATA_ANALYST")
				.orElseThrow(() -> new ResourceNotFoundException("Data Analyst category not found"));
	}

	// ==================== VALIDATION METHODS ====================

	public boolean isCategoryInUse(UUID categoryId) {
		return categoryRepository.countUsersByCategoryId(categoryId) > 0;
	}

	public boolean hasPermission(UUID categoryId, String permissionCode) {
		UserCategory category = getCategoryEntityById(categoryId);
		return category.getPermissions().stream()
				.anyMatch(p -> p.getCode().equals(permissionCode));
	}

	public List<String> getCategoryPermissionCodes(UUID categoryId) {
		UserCategory category = getCategoryEntityById(categoryId);
		return category.getPermissions().stream()
				.map(Permission::getCode)
				.collect(Collectors.toList());
	}

	// ==================== INITIALIZATION METHODS ====================

	@Transactional
	public void initializeDefaultCategories() {
		if (categoryRepository.count() == 0) {
			try {
				List<UserCategory> defaultCategories = List.of(
						UserCategory.builder()
								.label("ADMIN")
								.description("Administrator - Full access")
								.accessLevel(5)
								.build(),
						UserCategory.builder()
								.label("DATA_ANALYST")
								.description("Data Analyst - Full data access")
								.accessLevel(4)
								.build(),
						UserCategory.builder()
								.label("CONSULTANT")
								.description("Consultant - Read and export only")
								.accessLevel(2)
								.build(),
						UserCategory.builder()
								.label("INVITE")
								.description("Guest - Restricted read access")
								.accessLevel(1)
								.build()
				);

				categoryRepository.saveAll(defaultCategories);
				log.info("Default categories initialized successfully");
			} catch (Exception ex) {
				log.error("Error initializing default categories: {}", ex.getMessage(), ex);
				throw new DatabaseException("Failed to initialize default categories", ex);
			}
		}
	}

	@Transactional
	public void assignAllPermissionsToAdmin() {
		try {
			UserCategory admin = getAdminCategory();
			List<PermissionResponse> allPermissions = permissionService.getAllPermissions();
			admin.setPermissions(allPermissions.stream().map(permissionMapper::toEntity).collect(Collectors.toList()));
			categoryRepository.save(admin);
			log.info("All permissions assigned to ADMIN category");
		} catch (Exception ex) {
			log.error("Error assigning permissions to admin: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to assign permissions to admin", ex);
		}
	}
}