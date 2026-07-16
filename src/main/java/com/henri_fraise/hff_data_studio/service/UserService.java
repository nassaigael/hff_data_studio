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