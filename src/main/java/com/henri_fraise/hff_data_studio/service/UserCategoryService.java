package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.CategoryCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.entity.Permission;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.exception.ValidationException;
import com.henri_fraise.hff_data_studio.mapper.UserCategoryMapper;
import com.henri_fraise.hff_data_studio.repository.UserCategoryRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCategoryService {

  private final UserCategoryRepository categoryRepository;
  private final PermissionService permissionService;
  private final UserCategoryMapper categoryMapper;
  private final AuditLogService auditLogService;

  // ==================== CRUD Operations ====================

  public List<UserCategoryResponse> getAllCategories() {
    try {
      return categoryRepository.findAll().stream()
          .map(categoryMapper::toResponse)
          .collect(Collectors.toList());
    } catch (Exception ex) {
      log.error("Error retrieving categories: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve categories", ex);
    }
  }

  public UserCategoryResponse getCategoryById(UUID categoryId) {
    UserCategory category = getCategoryEntityById(categoryId);
    return categoryMapper.toResponse(category);
  }

  public UserCategory getCategoryEntityById(UUID categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("UserCategory", categoryId));
  }

  public UserCategory getCategoryEntityByLabel(String label) {
    return categoryRepository
        .findByLabel(label)
        .orElseThrow(() -> new ResourceNotFoundException("UserCategory", "label", label));
  }

  @Transactional
  public UserCategoryResponse createCategory(CategoryCreationRequest request) {
    // Check if label already exists
    if (categoryRepository.existsByLabel(request.getLabel())) {
      throw new ResourceAlreadyExistsException("UserCategory", "label", request.getLabel());
    }

    try {
      UserCategory category = categoryMapper.toEntity(request);
      UserCategory saved = categoryRepository.save(category);

      log.info("Category created successfully: {} ({})", saved.getLabel(), saved.getId());

      // Audit log
      auditLogService.logAction(
          "CATEGORY_CREATED",
          "UserCategory",
          saved.getId(),
          "Category " + saved.getLabel() + " created");

      return categoryMapper.toResponse(saved);
    } catch (Exception ex) {
      log.error("Error creating category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create category", ex);
    }
  }

  @Transactional
  public UserCategoryResponse updateCategory(UUID categoryId, CategoryCreationRequest request) {
    UserCategory category = getCategoryEntityById(categoryId);

    // Check label uniqueness if changed
    if (!category.getLabel().equals(request.getLabel())
        && categoryRepository.existsByLabel(request.getLabel())) {
      throw new ResourceAlreadyExistsException("UserCategory", "label", request.getLabel());
    }

    try {
      category.setLabel(request.getLabel());
      category.setDescription(request.getDescription());
      category.setAccessLevel(request.getAccessLevel());

      UserCategory updated = categoryRepository.save(category);
      log.info("Category updated successfully: {} ({})", updated.getLabel(), updated.getId());

      // Audit log
      auditLogService.logAction(
          "CATEGORY_UPDATED",
          "UserCategory",
          updated.getId(),
          "Category " + updated.getLabel() + " updated");

      return categoryMapper.toResponse(updated);
    } catch (Exception ex) {
      log.error("Error updating category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update category", ex);
    }
  }

  @Transactional
  public void deleteCategory(UUID categoryId) {
    UserCategory category = getCategoryEntityById(categoryId);

    // Check if category has users
    if (!category.getUsers().isEmpty()) {
      throw new ValidationException(
          "Cannot delete category with assigned users. Please reassign or deactivate users first.");
    }

    try {
      categoryRepository.delete(category);
      log.info("Category deleted successfully: {} ({})", category.getLabel(), categoryId);

      // Audit log
      auditLogService.logAction(
          "CATEGORY_DELETED",
          "UserCategory",
          categoryId,
          "Category " + category.getLabel() + " deleted");
    } catch (Exception ex) {
      log.error("Error deleting category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete category", ex);
    }
  }

  // ==================== Permission Management ====================

  @Transactional
  public void assignPermissionsToCategory(UUID categoryId, List<UUID> permissionIds) {
    UserCategory category = getCategoryEntityById(categoryId);

    try {
      List<Permission> permissions = permissionService.getPermissionEntitiesByIds(permissionIds);
      category.setPermissions(permissions);
      categoryRepository.save(category);

      log.info(
          "Permissions assigned to category {}: {}", category.getLabel(), permissionIds.size());

      // Audit log
      auditLogService.logAction(
          "PERMISSIONS_ASSIGNED",
          "UserCategory",
          categoryId,
          "Assigned " + permissionIds.size() + " permissions to category " + category.getLabel());
    } catch (Exception ex) {
      log.error("Error assigning permissions to category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to assign permissions to category", ex);
    }
  }

  public List<Permission> getPermissionsForCategory(UUID categoryId) {
    UserCategory category = getCategoryEntityById(categoryId);
    return category.getPermissions();
  }

  // ==================== Validation Methods ====================

  public boolean existsByLabel(String label) {
    return categoryRepository.existsByLabel(label);
  }

  public List<UserCategory> getCategoriesByAccessLevel(Integer maxAccessLevel) {
    return categoryRepository.findByMaxAccessLevel(maxAccessLevel);
  }
}
