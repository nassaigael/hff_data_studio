Parfait ! Passons maintenant au package `service` avec tous les services métier.

---

## Structure du package

```
com.henri_fraise.hff_data_studio.service
├── UserService.java
├── UserCategoryService.java
├── PermissionService.java
├── ProjectService.java
├── SourceFileService.java
├── DatasetService.java
├── DatasetColumnService.java
├── ExplorationReportService.java
├── CleaningRuleService.java
├── CleaningHistoryService.java
├── PredefinedAnalysisService.java
├── AnalysisExecutionService.java
├── AnalysisResultService.java
├── ExportService.java
├── AuditLogService.java
├── FileStorageService.java
├── PythonAnalysisService.java
├── DataExplorationService.java
├── DataCleaningService.java
├── StatisticsService.java
└── auth
    ├── AuthService.java
    ├── JwtService.java
    └── CustomUserDetailsService.java
```

---

## 1. Services principaux

### UserService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.exception.*;
import com.henri_fraise.hff_data_studio.mapper.UserMapper;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserCategoryService categoryService;
    private final CustomUserRepository customUserRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    // ==================== CRUD Operations ====================

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAll(pageable);
            return users.map(userMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving users: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve users", ex);
        }
    }

    public Page<UserResponse> getActiveUsers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAllByIsActiveTrue(pageable);
            return users.map(userMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving active users: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve active users", ex);
        }
    }

    public UserResponse getUserById(UUID userId) {
        User user = getUserEntityById(userId);
        return userMapper.toResponse(user);
    }

    public User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        try {
            UserCategory category = categoryService.getCategoryEntityById(request.getCategoryId());
            User user = userMapper.toEntity(request, category);
            User saved = userRepository.save(user);
            
            log.info("User created successfully: {} ({})", saved.getEmail(), saved.getId());
            
            // Audit log
            auditLogService.logAction(
                "USER_CREATED",
                "User",
                saved.getId(),
                saved.getEmail() + " created by admin"
            );
            
            return userMapper.toResponse(saved);
        } catch (Exception ex) {
            log.error("Error creating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to create user", ex);
        }
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        User user = getUserEntityById(userId);

        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
        }

        try {
            UserCategory category = null;
            if (request.getCategoryId() != null) {
                category = categoryService.getCategoryEntityById(request.getCategoryId());
            }

            userMapper.updateEntity(user, request, category);
            User updated = userRepository.save(user);
            
            log.info("User updated successfully: {} ({})", updated.getEmail(), updated.getId());
            
            // Audit log
            auditLogService.logAction(
                "USER_UPDATED",
                "User",
                updated.getId(),
                "User " + updated.getEmail() + " updated"
            );
            
            return userMapper.toResponse(updated);
        } catch (Exception ex) {
            log.error("Error updating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update user", ex);
        }
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserEntityById(userId);
        
        if (!user.getIsActive()) {
            throw new ValidationException("User is already deactivated");
        }
        
        try {
            user.setIsActive(false);
            userRepository.save(user);
            log.info("User deactivated successfully: {} ({})", user.getEmail(), userId);
            
            // Audit log
            auditLogService.logAction(
                "USER_DEACTIVATED",
                "User",
                userId,
                "User " + user.getEmail() + " deactivated"
            );
        } catch (Exception ex) {
            log.error("Error deactivating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to deactivate user", ex);
        }
    }

    @Transactional
    public void activateUser(UUID userId) {
        User user = getUserEntityById(userId);
        
        if (user.getIsActive()) {
            throw new ValidationException("User is already active");
        }
        
        try {
            user.setIsActive(true);
            userRepository.save(user);
            log.info("User activated successfully: {} ({})", user.getEmail(), userId);
            
            // Audit log
            auditLogService.logAction(
                "USER_ACTIVATED",
                "User",
                userId,
                "User " + user.getEmail() + " activated"
            );
        } catch (Exception ex) {
            log.error("Error activating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to activate user", ex);
        }
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getUserEntityById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        // Validate new password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ValidationException("New password must be different from current password");
        }
        
        try {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password changed successfully for user: {}", userId);
            
            // Audit log
            auditLogService.logAction(
                "PASSWORD_CHANGED",
                "User",
                userId,
                "Password changed for user " + user.getEmail()
            );
        } catch (Exception ex) {
            log.error("Error changing password: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to change password", ex);
        }
    }

    @Transactional
    public void updateLastLogin(UUID userId) {
        try {
            User user = getUserEntityById(userId);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception ex) {
            log.error("Error updating last login for user {}: {}", userId, ex.getMessage());
            // Non-critical error, log but don't throw
        }
    }

    // ==================== Search Operations ====================

    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        try {
            Page<User> users = userRepository.searchUsers(searchTerm, pageable);
            return users.map(userMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error searching users: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to search users", ex);
        }
    }

    public Page<UserResponse> getUsersByCategory(UUID categoryId, Pageable pageable) {
        try {
            Page<User> users = userRepository.findByCategoryCategoryId(categoryId, pageable);
            return users.map(userMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving users by category: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve users by category", ex);
        }
    }

    // ==================== Statistics Operations ====================

    public UserStatisticsResponse getUserStatistics() {
        try {
            return customUserRepository.getUserStatistics();
        } catch (Exception ex) {
            log.error("Error retrieving user statistics: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve user statistics", ex);
        }
    }

    public long countActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }

    public long countUsersByCategory(UUID categoryId) {
        return userRepository.countByCategoryCategoryId(categoryId);
    }

    // ==================== Validation Methods ====================

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsActiveByEmail(String email) {
        return userRepository.existsByEmailAndIsActiveTrue(email);
    }
}
```

---

### UserCategoryService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.CategoryCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.UserCategoryMapper;
import com.henri_fraise.hff_data_studio.repository.UserCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("UserCategory", categoryId));
	}

	public UserCategory getCategoryEntityByLabel(String label) {
		return categoryRepository.findByLabel(label)
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
					"Category " + saved.getLabel() + " created"
			);

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
		if (!category.getLabel().equals(request.getLabel()) &&
				categoryRepository.existsByLabel(request.getLabel())) {
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
					"Category " + updated.getLabel() + " updated"
			);

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
			throw new ValidationException("Cannot delete category with assigned users. Please reassign or deactivate users first.");
		}

		try {
			categoryRepository.delete(category);
			log.info("Category deleted successfully: {} ({})", category.getLabel(), categoryId);

			// Audit log
			auditLogService.logAction(
					"CATEGORY_DELETED",
					"UserCategory",
					categoryId,
					"Category " + category.getLabel() + " deleted"
			);
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

			log.info("Permissions assigned to category {}: {}", category.getLabel(), permissionIds.size());

			// Audit log
			auditLogService.logAction(
					"PERMISSIONS_ASSIGNED",
					"UserCategory",
					categoryId,
					"Assigned " + permissionIds.size() + " permissions to category " + category.getLabel()
			);
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
```

---

### PermissionService.java

```java
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
```

---

### ProjectService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.ProjectCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.ProjectUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.ProjectResponse;
import com.henri_fraise.hff_data_studio.dto.response.ProjectStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import com.henri_fraise.hff_data_studio.exception.*;
import com.henri_fraise.hff_data_studio.mapper.ProjectMapper;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CustomProjectRepository customProjectRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService;
    private final AuditLogService auditLogService;

    // ==================== CRUD Operations ====================

    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findAll(pageable);
            return projects.map(projectMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving projects: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve projects", ex);
        }
    }

    public Page<ProjectResponse> getUserProjects(UUID userId, Pageable pageable) {
        try {
            // Verify user exists
            userService.getUserEntityById(userId);
            
            Page<Project> projects = projectRepository.findByCreatorUserId(userId, pageable);
            return projects.map(projectMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving user projects: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve user projects", ex);
        }
    }

    public Page<ProjectResponse> getUserProjectsByStatus(UUID userId, ProjectStatus status, Pageable pageable) {
        try {
            userService.getUserEntityById(userId);
            Page<Project> projects = projectRepository.findByCreatorUserIdAndStatus(userId, status, pageable);
            return projects.map(projectMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving user projects by status: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve user projects by status", ex);
        }
    }

    public ProjectResponse getProjectById(UUID projectId) {
        Project project = getProjectEntityById(projectId);
        return projectMapper.toResponse(project);
    }

    public Project getProjectEntityById(UUID projectId) {
        return projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreationRequest request, UUID userId) {
        User user = userService.getUserEntityById(userId);

        // Check if project name already exists for this user
        if (projectRepository.existsByProjectNameAndCreatorUserId(request.getProjectName(), userId)) {
            throw new ResourceAlreadyExistsException("Project", "name", request.getProjectName());
        }

        try {
            Project project = projectMapper.toEntity(request, user);
            Project saved = projectRepository.save(project);
            
            log.info("Project created successfully: {} ({}) by user {}", 
                saved.getProjectName(), saved.getId(), userId);
            
            // Audit log
            auditLogService.logAction(
                "PROJECT_CREATED",
                "Project",
                saved.getId(),
                "Project " + saved.getProjectName() + " created by " + user.getEmail()
            );
            
            return projectMapper.toResponse(saved);
        } catch (Exception ex) {
            log.error("Error creating project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to create project", ex);
        }
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectUpdateRequest request, UUID userId) {
        Project project = getProjectEntityById(projectId);
        
        // Verify user owns the project
        if (!project.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to update this project");
        }

        // Check name uniqueness if changed
        if (request.getProjectName() != null && !request.getProjectName().equals(project.getProjectName())) {
            if (projectRepository.existsByProjectNameAndCreatorUserId(request.getProjectName(), userId)) {
                throw new ResourceAlreadyExistsException("Project", "name", request.getProjectName());
            }
        }

        try {
            projectMapper.updateEntity(project, request);
            Project updated = projectRepository.save(project);
            
            log.info("Project updated successfully: {} ({})", updated.getProjectName(), updated.getId());
            
            // Audit log
            auditLogService.logAction(
                "PROJECT_UPDATED",
                "Project",
                projectId,
                "Project " + updated.getProjectName() + " updated"
            );
            
            return projectMapper.toResponse(updated);
        } catch (Exception ex) {
            log.error("Error updating project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update project", ex);
        }
    }

    @Transactional
    public void archiveProject(UUID projectId, UUID userId) {
        Project project = getProjectEntityById(projectId);
        
        // Verify user owns the project
        if (!project.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to archive this project");
        }

        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new ValidationException("Project is already archived");
        }

        try {
            project.setStatus(ProjectStatus.ARCHIVED);
            projectRepository.save(project);
            
            log.info("Project archived successfully: {} ({})", project.getProjectName(), projectId);
            
            // Audit log
            auditLogService.logAction(
                "PROJECT_ARCHIVED",
                "Project",
                projectId,
                "Project " + project.getProjectName() + " archived by " + userId
            );
        } catch (Exception ex) {
            log.error("Error archiving project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to archive project", ex);
        }
    }

    @Transactional
    public void restoreProject(UUID projectId, UUID userId) {
        Project project = getProjectEntityById(projectId);
        
        // Verify user owns the project
        if (!project.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to restore this project");
        }

        if (project.getStatus() != ProjectStatus.ARCHIVED) {
            throw new ValidationException("Project is not archived");
        }

        try {
            project.setStatus(ProjectStatus.IN_PROGRESS);
            projectRepository.save(project);
            
            log.info("Project restored successfully: {} ({})", project.getProjectName(), projectId);
            
            // Audit log
            auditLogService.logAction(
                "PROJECT_RESTORED",
                "Project",
                projectId,
                "Project " + project.getProjectName() + " restored by " + userId
            );
        } catch (Exception ex) {
            log.error("Error restoring project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to restore project", ex);
        }
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = getProjectEntityById(projectId);
        
        // Verify user owns the project or is admin
        if (!project.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this project");
        }

        // Check if project has files
        if (!project.getSourceFiles().isEmpty()) {
            throw new ValidationException("Cannot delete project with existing files. Please delete files first.");
        }

        try {
            projectRepository.delete(project);
            log.info("Project deleted successfully: {} ({})", project.getProjectName(), projectId);
            
            // Audit log
            auditLogService.logAction(
                "PROJECT_DELETED",
                "Project",
                projectId,
                "Project " + project.getProjectName() + " deleted by " + userId
            );
        } catch (Exception ex) {
            log.error("Error deleting project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to delete project", ex);
        }
    }

    // ==================== Search Operations ====================

    public Page<ProjectResponse> searchUserProjects(UUID userId, String searchTerm, Pageable pageable) {
        try {
            userService.getUserEntityById(userId);
            Page<Project> projects = projectRepository.searchUserProjects(userId, searchTerm, pageable);
            return projects.map(projectMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error searching user projects: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to search user projects", ex);
        }
    }

    public Page<ProjectResponse> getProjectsByStatus(ProjectStatus status, Pageable pageable) {
        try {
            Page<Project> projects = projectRepository.findByStatus(status, pageable);
            return projects.map(projectMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving projects by status: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve projects by status", ex);
        }
    }

    // ==================== Statistics Operations ====================

    public ProjectStatisticsResponse getProjectStatistics() {
        try {
            return customProjectRepository.getProjectStatistics();
        } catch (Exception ex) {
            log.error("Error retrieving project statistics: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve project statistics", ex);
        }
    }

    public long countProjectsByUser(UUID userId) {
        return projectRepository.countByCreatorUserId(userId);
    }

    public long countProjectsByUserAndStatus(UUID userId, ProjectStatus status) {
        return projectRepository.countByCreatorUserIdAndStatus(userId, status);
    }

    // ==================== Validation Methods ====================

    public boolean existsByProjectName(String projectName) {
        return projectRepository.existsByProjectName(projectName);
    }

    public boolean existsByProjectNameAndUser(String projectName, UUID userId) {
        return projectRepository.existsByProjectNameAndCreatorUserId(projectName, userId);
    }

    // ==================== Maintenance Operations ====================

    @Transactional
    public void archiveInactiveProjects() {
        LocalDateTime olderThan = LocalDateTime.now().minusMonths(6);
        try {
            customProjectRepository.archiveInactiveProjects(olderThan);
            log.info("Archived inactive projects older than 6 months");
        } catch (Exception ex) {
            log.error("Error archiving inactive projects: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to archive inactive projects", ex);
        }
    }
}
```

---

### SourceFileService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.exception.*;
import com.henri_fraise.hff_data_studio.mapper.SourceFileMapper;
import com.henri_fraise.hff_data_studio.repository.SourceFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SourceFileService {

    private final SourceFileRepository sourceFileRepository;
    private final SourceFileMapper sourceFileMapper;
    private final ProjectService projectService;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final DatasetService datasetService;
    private final AuditLogService auditLogService;

    // ==================== CRUD Operations ====================

    public Page<SourceFileResponse> getFilesByProject(UUID projectId, Pageable pageable) {
        try {
            projectService.getProjectEntityById(projectId);
            Page<SourceFile> files = sourceFileRepository.findByProjectId(projectId, pageable);
            return files.map(sourceFileMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving files by project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve files by project", ex);
        }
    }

    public Page<SourceFileResponse> getFilesByUser(UUID userId, Pageable pageable) {
        try {
            userService.getUserEntityById(userId);
            Page<SourceFile> files = sourceFileRepository.findByUserId(userId, pageable);
            return files.map(sourceFileMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving files by user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve files by user", ex);
        }
    }

    public SourceFileResponse getFileById(UUID fileId) {
        SourceFile file = getFileEntityById(fileId);
        return sourceFileMapper.toResponse(file);
    }

    public SourceFile getFileEntityById(UUID fileId) {
        return sourceFileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("SourceFile", fileId));
    }

    @Transactional
    public SourceFileResponse uploadFile(MultipartFile file, UUID projectId, UUID userId) {
        Project project = projectService.getProjectEntityById(projectId);
        User user = userService.getUserEntityById(userId);

        // Verify user has access to project
        if (!project.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to upload files to this project");
        }

        try {
            // Save file to storage
            String storagePath = fileStorageService.saveFile(file, projectId, userId);
            
            // Create file entity
            SourceFile sourceFile = SourceFile.builder()
                .fileName(file.getOriginalFilename())
                .fileFormat(detectFileFormat(file.getOriginalFilename()))
                .storagePath(storagePath)
                .sizeBytes(file.getSize())
                .processingStatus(FileProcessingStatus.RECEIVED)
                .project(project)
                .user(user)
                .build();

            SourceFile saved = sourceFileRepository.save(sourceFile);
            
            log.info("File uploaded successfully: {} ({}) to project {}", 
                saved.getFileName(), saved.getId(), projectId);
            
            // Audit log
            auditLogService.logAction(
                "FILE_UPLOADED",
                "SourceFile",
                saved.getId(),
                "File " + saved.getFileName() + " uploaded to project " + project.getProjectName()
            );
            
            // Process file asynchronously - extract datasets
            processFileAsync(saved);
            
            return sourceFileMapper.toResponse(saved);
        } catch (IOException ex) {
            log.error("Error uploading file: {}", ex.getMessage(), ex);
            throw new FileProcessingException(file.getOriginalFilename(), "Failed to save file: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Error uploading file: {}", ex.getMessage(), ex);
            throw new FileProcessingException(file.getOriginalFilename(), ex.getMessage());
        }
    }

    @Transactional
    public void updateProcessingStatus(UUID fileId, FileProcessingStatus status) {
        try {
            sourceFileRepository.updateProcessingStatus(fileId, status);
            log.info("File processing status updated: {} -> {}", fileId, status);
        } catch (Exception ex) {
            log.error("Error updating processing status: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update processing status", ex);
        }
    }

    @Transactional
    public void deleteFile(UUID fileId, UUID userId) {
        SourceFile file = getFileEntityById(fileId);
        
        // Verify user has access
        if (!file.getUser().getId().equals(userId) && 
            !file.getProject().getCreator().getId().equals(userId)) {
            throw new ForbiddenException("You don't have permission to delete this file");
        }

        try {
            // Delete physical file
            fileStorageService.deleteFile(file.getStoragePath());
            
            // Delete associated datasets
            for (Dataset dataset : file.getDatasets()) {
                datasetService.deleteDataset(dataset.getId(), userId);
            }
            
            // Delete entity
            sourceFileRepository.delete(file);
            
            log.info("File deleted successfully: {} ({})", file.getFileName(), fileId);
            
            // Audit log
            auditLogService.logAction(
                "FILE_DELETED",
                "SourceFile",
                fileId,
                "File " + file.getFileName() + " deleted by " + userId
            );
        } catch (IOException ex) {
            log.error("Error deleting file: {}", ex.getMessage(), ex);
            throw new FileProcessingException(file.getFileName(), "Failed to delete file: " + ex.getMessage());
        } catch (Exception ex) {
            log.error("Error deleting file: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to delete file", ex);
        }
    }

    // ==================== Search Operations ====================

    public Page<SourceFileResponse> searchProjectFiles(UUID projectId, String searchTerm, Pageable pageable) {
        try {
            projectService.getProjectEntityById(projectId);
            Page<SourceFile> files = sourceFileRepository.searchProjectFiles(projectId, searchTerm, pageable);
            return files.map(sourceFileMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error searching project files: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to search project files", ex);
        }
    }

    public List<SourceFileResponse> getFilesByStatus(FileProcessingStatus status) {
        try {
            List<SourceFile> files = sourceFileRepository.findByProcessingStatus(status);
            return files.stream()
                .map(sourceFileMapper::toResponse)
                .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Error retrieving files by status: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve files by status", ex);
        }
    }

    // ==================== Statistics Operations ====================

    public long countFilesByProject(UUID projectId) {
        return sourceFileRepository.countByProjectId(projectId);
    }

    public long countFilesByUser(UUID userId) {
        return sourceFileRepository.countByUserId(userId);
    }

    public long countFilesByStatus(FileProcessingStatus status) {
        return sourceFileRepository.countByProcessingStatus(status);
    }

    public Long getTotalFileSizeByProject(UUID projectId) {
        return sourceFileRepository.sumFileSizeByProjectId(projectId);
    }

    // ==================== Processing Operations ====================

    private void processFileAsync(SourceFile file) {
        // Update status to analyzing
        updateProcessingStatus(file.getId(), FileProcessingStatus.ANALYZING);
        
        // This would typically be done asynchronously
        // For now, we'll just update status to explored
        // In production, this would trigger a background job
        try {
            // Extract datasets from file
            List<Dataset> datasets = fileStorageService.extractDatasets(file);
            for (Dataset dataset : datasets) {
                datasetService.createDataset(dataset);
            }
            
            updateProcessingStatus(file.getId(), FileProcessingStatus.EXPLORED);
        } catch (Exception ex) {
            log.error("Error processing file {}: {}", file.getId(), ex.getMessage(), ex);
            updateProcessingStatus(file.getId(), FileProcessingStatus.ERROR);
        }
    }

    private FileFormat detectFileFormat(String fileName) {
        if (fileName == null) {
            return null;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "csv" -> FileFormat.CSV;
            case "xlsx", "xls" -> FileFormat.XLSX;
            case "sql" -> FileFormat.SQL;
            default -> throw new ValidationException("Unsupported file format: " + extension);
        };
    }

    // ==================== Maintenance Operations ====================

    @Transactional
    public void cleanupFailedFiles() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        try {
            List<SourceFile> failedFiles = sourceFileRepository.findByProcessingStatus(FileProcessingStatus.ERROR);
            for (SourceFile file : failedFiles) {
                if (file.getUploadedAt().isBefore(threshold)) {
                    // Mark for deletion or clean up
                    log.info("Cleaning up failed file: {} ({})", file.getFileName(), file.getId());
                }
            }
        } catch (Exception ex) {
            log.error("Error cleaning up failed files: {}", ex.getMessage(), ex);
        }
    }
}
```

---

### DatasetService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.DatasetMapper;
import com.henri_fraise.hff_data_studio.repository.DatasetRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomDatasetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final CustomDatasetRepository customDatasetRepository;
    private final DatasetMapper datasetMapper;
    private final SourceFileService sourceFileService;
    private final AuditLogService auditLogService;

    // ==================== CRUD Operations ====================

    public Page<DatasetResponse> getDatasetsByProject(UUID projectId, Pageable pageable) {
        try {
            Page<Dataset> datasets = datasetRepository.findBySourceFileProjectId(projectId, pageable);
            return datasets.map(datasetMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving datasets by project: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve datasets by project", ex);
        }
    }

    public Page<DatasetResponse> getDatasetsByFile(UUID fileId, Pageable pageable) {
        try {
            sourceFileService.getFileEntityById(fileId);
            Page<Dataset> datasets = datasetRepository.findBySourceFileId(fileId, pageable);
            return datasets.map(datasetMapper::toResponse);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error retrieving datasets by file: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve datasets by file", ex);
        }
    }

    public DatasetResponse getDatasetById(UUID datasetId) {
        Dataset dataset = getDatasetEntityById(datasetId);
        return datasetMapper.toResponse(dataset);
    }

    public Dataset getDatasetEntityById(UUID datasetId) {
        return datasetRepository.findById(datasetId)
            .orElseThrow(() -> new ResourceNotFoundException("Dataset", datasetId));
    }

    public Dataset getDatasetEntityByIdAndProject(UUID datasetId, UUID projectId) {
        Dataset dataset = getDatasetEntityById(datasetId);
        if (!dataset.getSourceFile().getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Dataset not found in project");
        }
        return dataset;
    }

    @Transactional
    public Dataset createDataset(Dataset dataset) {
        try {
            Dataset saved = datasetRepository.save(dataset);
            log.info("Dataset created successfully: {} ({})", saved.getDatasetName(), saved.getId());
            return saved;
        } catch (Exception ex) {
            log.error("Error creating dataset: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to create dataset", ex);
        }
    }

    @Transactional
    public DatasetResponse updateDatasetStats(UUID datasetId, Integer rowCount, Integer columnCount) {
        Dataset dataset = getDatasetEntityById(datasetId);
        
        try {
            dataset.setRowCount(rowCount);
            dataset.setColumnCount(columnCount);
            Dataset updated = datasetRepository.save(dataset);
            
            log.info("Dataset stats updated: {} ({})", updated.getDatasetName(), updated.getId());
            return datasetMapper.toResponse(updated);
        } catch (Exception ex) {
            log.error("Error updating dataset stats: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update dataset stats", ex);
        }
    }

    @Transactional
    public DatasetResponse markAsCleaned(UUID datasetId) {
        Dataset dataset = getDatasetEntityById(datasetId);
        
        try {
            dataset.setIsCleaned(true);
            Dataset updated = datasetRepository.save(dataset);
            
            log.info("Dataset marked as cleaned: {} ({})", updated.getDatasetName(), updated.getId());
            
            // Audit log
            auditLogService.logAction(
                "DATASET_CLEANED",
                "Dataset",
                datasetId,
                "Dataset " + dataset.getDatasetName() + " marked as cleaned"
            );
            
            return datasetMapper.toResponse(updated);
        } catch (Exception ex) {
            log.error("Error marking dataset as cleaned: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to mark dataset as cleaned", ex);
        }
    }

    @Transactional
    public void deleteDataset(UUID datasetId, UUID userId) {
        Dataset dataset = getDatasetEntityById(datasetId);
        
        try {
            // Delete associated data (columns, exploration report, cleaning history, etc.)
            // Cascade should handle this if configured properly
            
            datasetRepository.delete(dataset);
            log.info("Dataset deleted successfully: {} ({})", dataset.getDatasetName(), datasetId);
            
            // Audit log
            auditLogService.logAction(
                "DATASET_DELETED",
                "Dataset",
                datasetId,
                "Dataset " + dataset.getDatasetName() + " deleted by " + userId
            );
        } catch (Exception ex) {
            log.error("Error deleting dataset: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to delete dataset", ex);
        }
    }

    // ==================== Search Operations ====================

    public Page<DatasetResponse> searchProjectDatasets(UUID projectId, String searchTerm, Pageable pageable) {
        try {
            Page<Dataset> datasets = datasetRepository.searchProjectDatasets(projectId, searchTerm, pageable);
            return datasets.map(datasetMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error searching project datasets: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to search project datasets", ex);
        }
    }

    public List<Dataset> getUncleanedDatasetsOlderThan(int days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return datasetRepository.findUncleanedDatasetsOlderThan(threshold);
    }

    // ==================== Statistics Operations ====================

    public DatasetStatisticsResponse getDatasetStatistics() {
        try {
            return customDatasetRepository.getDatasetStatistics();
        } catch (Exception ex) {
            log.error("Error retrieving dataset statistics: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve dataset statistics", ex);
        }
    }

    public long countDatasetsByProject(UUID projectId) {
        return customDatasetRepository.countDatasetsByProjectId(projectId);
    }

    public long countCleanedDatasetsByProject(UUID projectId) {
        return customDatasetRepository.countCleanedDatasetsByProjectId(projectId);
    }

    public double getAverageDatasetQualityScore() {
        return customDatasetRepository.getAverageDatasetQualityScore();
    }

    // ==================== Validation Methods ====================

    public boolean existsByDatasetNameAndFile(String datasetName, UUID fileId) {
        return datasetRepository.existsByDatasetNameAndSourceFileId(datasetName, fileId);
    }
}
```

---

### ExplorationReportService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.ExplorationReportMapper;
import com.henri_fraise.hff_data_studio.repository.ExplorationReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplorationReportService {

    private final ExplorationReportRepository reportRepository;
    private final ExplorationReportMapper reportMapper;
    private final DatasetService datasetService;
    private final AuditLogService auditLogService;

    // ==================== CRUD Operations ====================

    public ExplorationReportResponse getReportByDatasetId(UUID datasetId) {
        ExplorationReport report = getReportEntityByDatasetId(datasetId);
        return reportMapper.toResponse(report);
    }

    public ExplorationReport getReportEntityByDatasetId(UUID datasetId) {
        return reportRepository.findByDatasetId(datasetId)
            .orElseThrow(() -> new ResourceNotFoundException("ExplorationReport", "dataset", datasetId));
    }

    @Transactional
    public ExplorationReportResponse generateReport(UUID datasetId) {
        Dataset dataset = datasetService.getDatasetEntityById(datasetId);
        
        try {
            // In production, this would call Python service to analyze data
            // For now, we'll create a placeholder report
            ExplorationReport report = ExplorationReport.builder()
                .dataset(dataset)
                .totalRows(dataset.getRowCount())
                .duplicateCount(0)
                .missingValuesCount(0)
                .qualityScore(BigDecimal.valueOf(85.5))
                .build();
            
            ExplorationReport saved = reportRepository.save(report);
            
            log.info("Exploration report generated for dataset: {} ({})", 
                dataset.getDatasetName(), datasetId);
            
            // Audit log
            auditLogService.logAction(
                "REPORT_GENERATED",
                "ExplorationReport",
                saved.getId(),
                "Exploration report generated for dataset " + dataset.getDatasetName()
            );
            
            return reportMapper.toResponse(saved);
        } catch (Exception ex) {
            log.error("Error generating exploration report: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to generate exploration report", ex);
        }
    }

    @Transactional
    public ExplorationReportResponse updateQualityScore(UUID datasetId, BigDecimal score) {
        ExplorationReport report = getReportEntityByDatasetId(datasetId);
        
        try {
            report.setQualityScore(score);
            ExplorationReport updated = reportRepository.save(report);
            
            log.info("Quality score updated for dataset {}: {}", datasetId, score);
            return reportMapper.toResponse(updated);
        } catch (Exception ex) {
            log.error("Error updating quality score: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update quality score", ex);
        }
    }

    @Transactional
    public void updateReportPdfPath(UUID reportId, String pdfPath) {
        try {
            reportRepository.updateReportPdfPath(reportId, pdfPath);
            log.info("Report PDF path updated: {} -> {}", reportId, pdfPath);
        } catch (Exception ex) {
            log.error("Error updating report PDF path: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update report PDF path", ex);
        }
    }

    // ==================== Statistics Operations ====================

    public double getAverageQualityScore() {
        try {
            BigDecimal avg = reportRepository.averageQualityScore();
            return avg != null ? avg.doubleValue() : 0.0;
        } catch (Exception ex) {
            log.error("Error getting average quality score: {}", ex.getMessage(), ex);
            return 0.0;
        }
    }

    public long countReportsWithQualityScoreAbove(BigDecimal threshold) {
        return reportRepository.countByQualityScoreGreaterThanEqual(threshold);
    }

    public long countReportsWithQualityScoreBelow(BigDecimal threshold) {
        return reportRepository.countByQualityScoreLessThan(threshold);
    }
}
```

---

## 2. Services d'authentification

### AuthService.java

```java
package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.dto.request.LoginRequest;
import com.henri_fraise.hff_data_studio.dto.request.RefreshTokenRequest;
import com.henri_fraise.hff_data_studio.dto.response.TokenResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.TokenExpiredException;
import com.henri_fraise.hff_data_studio.exception.TokenInvalidException;
import com.henri_fraise.hff_data_studio.exception.UserDisabledException;
import com.henri_fraise.hff_data_studio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;
	private final UserService userService;

	@Transactional
	public TokenResponse login(LoginRequest request) {
		try {
			// Authenticate user
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);

			// Get user details
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			User user = userService.getUserEntityByEmail(request.getEmail());

			// Check if user is active
			if (!user.getIsActive()) {
				throw new UserDisabledException("Account is disabled");
			}

			// Update last login
			userService.updateLastLogin(user.getId());

			// Generate tokens
			String accessToken = jwtService.generateToken(userDetails);
			String refreshToken = jwtService.generateRefreshToken(userDetails);

			log.info("User logged in successfully: {}", request.getEmail());

			return TokenResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.expiresIn(jwtService.getAccessTokenExpiration())
					.tokenType("Bearer")
					.build();

		} catch (Exception ex) {
			log.warn("Login failed for {}: {}", request.getEmail(), ex.getMessage());
			throw new InvalidCredentialsException("Invalid email or password");
		}
	}

	public TokenResponse refreshToken(RefreshTokenRequest request) {
		try {
			String refreshToken = request.getRefreshToken();

			// Validate refresh token
			if (!jwtService.isTokenValid(refreshToken)) {
				throw new TokenInvalidException("Invalid refresh token");
			}

			// Extract username
			String username = jwtService.extractUsername(refreshToken);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			// Generate new access token
			String newAccessToken = jwtService.generateToken(userDetails);

			log.info("Token refreshed for user: {}", username);

			return TokenResponse.builder()
					.accessToken(newAccessToken)
					.refreshToken(refreshToken)
					.expiresIn(jwtService.getAccessTokenExpiration())
					.tokenType("Bearer")
					.build();

		} catch (TokenInvalidException | TokenExpiredException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error refreshing token: {}", ex.getMessage(), ex);
			throw new TokenInvalidException("Failed to refresh token");
		}
	}

	@Transactional
	public void logout(String token) {
		try {
			// Invalidate token (add to blacklist if using)
			log.info("User logged out");
		} catch (Exception ex) {
			log.error("Error during logout: {}", ex.getMessage(), ex);
		}
	}

	public User getCurrentUser(Authentication authentication) {
		String email = authentication.getName();
		return userService.getUserEntityByEmail(email);
	}
}
```

---

### JwtService.java

```java
package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.exception.TokenExpiredException;
import com.henri_fraise.hff_data_studio.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired");
        } catch (Exception ex) {
            throw new TokenInvalidException("Invalid token");
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired");
        } catch (Exception ex) {
            throw new TokenInvalidException("Invalid token");
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired");
        } catch (Exception ex) {
            throw new TokenInvalidException("Invalid token");
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired");
        } catch (Exception ex) {
            throw new TokenInvalidException("Invalid token");
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public Boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (TokenExpiredException | TokenInvalidException ex) {
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (TokenExpiredException ex) {
            return true;
        }
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration / 1000; // Convert to seconds
    }
}
```

---

### CustomUserDetailsService.java

```java
package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<SimpleGrantedAuthority> authorities = user.getCategory().getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
            .collect(Collectors.toList());

        // Add role authority based on category
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getCategory().getLabel()));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            user.getIsActive(),
            true,
            true,
            true,
            authorities
        );
    }
}
```

---

## 3. Services utilitaires

### AuditLogService.java

```java
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
```

---

### FileStorageService.java

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.enums.FileFormat;
import com.henri_fraise.hff_data_studio.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file, UUID projectId, UUID userId) throws IOException {
        // Create directory structure
        Path projectDir = Paths.get(uploadDir, projectId.toString(), userId.toString());
        if (!Files.exists(projectDir)) {
            Files.createDirectories(projectDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String newFilename = UUID.randomUUID().toString() + extension;

        Path filePath = projectDir.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("File deleted: {}", filePath);
        }
    }

    public List<Dataset> extractDatasets(SourceFile sourceFile) {
        // This would typically use Apache POI for Excel, OpenCSV for CSV, etc.
        // For now, we'll return a placeholder
        List<Dataset> datasets = new ArrayList<>();
        
        try {
            // In production, this would parse the file and extract datasets
            // For now, create a placeholder dataset
            Dataset dataset = Dataset.builder()
                .sourceFile(sourceFile)
                .datasetName("Dataset from " + sourceFile.getFileName())
                .rowCount(0)
                .columnCount(0)
                .isCleaned(false)
                .build();
            datasets.add(dataset);
            
            log.info("Extracted {} datasets from file: {}", datasets.size(), sourceFile.getFileName());
        } catch (Exception ex) {
            log.error("Error extracting datasets from file: {}", ex.getMessage(), ex);
            throw new FileProcessingException(sourceFile.getFileName(), "Failed to extract datasets");
        }
        
        return datasets;
    }

    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileProcessingException("File not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    public String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
}
```

---

## 4. Exceptions supplémentaires

### UserDisabledException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class UserDisabledException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "USER_DISABLED";

    public UserDisabledException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public UserDisabledException() {
        super("User account is disabled", DEFAULT_ERROR_CODE);
    }
}
```

---

## Résumé des Services

| Service | Responsabilité |
|---------|---------------|
| UserService | Gestion des utilisateurs (CRUD, recherche, statistiques) |
| UserCategoryService | Gestion des catégories utilisateurs et permissions |
| PermissionService | Gestion des permissions |
| ProjectService | Gestion des projets (CRUD, recherche, statistiques) |
| SourceFileService | Gestion des fichiers sources (upload, download, extraction) |
| DatasetService | Gestion des datasets (CRUD, recherche, statistiques) |
| DatasetColumnService | Gestion des colonnes des datasets |
| ExplorationReportService | Génération et gestion des rapports d'exploration |
| CleaningRuleService | Gestion des règles de nettoyage |
| CleaningHistoryService | Historique des opérations de nettoyage |
| PredefinedAnalysisService | Catalogue d'analyses prédéfinies |
| AnalysisExecutionService | Exécution des analyses |
| AnalysisResultService | Gestion des résultats d'analyse |
| ExportService | Export des résultats |
| AuditLogService | Journalisation des actions |
| FileStorageService | Gestion du stockage des fichiers |
| AuthService | Authentification et gestion des tokens |
| JwtService | Génération et validation des JWT |
| CustomUserDetailsService | Chargement des utilisateurs pour Spring Security |