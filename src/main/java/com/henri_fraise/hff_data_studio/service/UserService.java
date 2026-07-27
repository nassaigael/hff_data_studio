package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.request.UserUpdateRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.dto.response.UserStatisticsResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.InvalidCredentialsException;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.exception.ValidationException;
import com.henri_fraise.hff_data_studio.mapper.UserMapper;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.repository.custom.CustomUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public User getUserEntityById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
  }

  public User getUserEntityByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
  }

  public UserResponse getUserById(UUID userId) {
    User user = getUserEntityById(userId);
    return userMapper.toResponse(user);
  }

  public UserResponse getUserByEmail(String email) {
    User user = getUserEntityByEmail(email);
    return userMapper.toResponse(user);
  }

  public Page<UserResponse> getAllUsers(Pageable pageable) {
    try {
      Page<User> users = userRepository.findAll(pageable);
      return users.map(userMapper::toResponse);
    } catch (Exception ex) {
      log.error("Error retrieving users: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve users", ex);
    }
  }

  public List<UserResponse> getAllUsers() {
    try {
      List<User> users = userRepository.findAll();
      return users.stream().map(userMapper::toResponse).toList();
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

  public List<UserResponse> getActiveUsers() {
    try {
      List<User> users = userRepository.findByIsActiveTrue();
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error retrieving active users: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve active users", ex);
    }
  }

  public List<UserResponse> getInactiveUsers() {
    try {
      List<User> users = userRepository.findByIsActiveFalse();
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error retrieving inactive users: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve inactive users", ex);
    }
  }

  public Page<UserResponse> getUsersByCategory(UUID categoryId, Pageable pageable) {
    try {
      Page<User> users = userRepository.findByCategoryId(categoryId, pageable);
      return users.map(userMapper::toResponse);
    } catch (Exception ex) {
      log.error("Error retrieving users by category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve users by category", ex);
    }
  }

  public List<UserResponse> getUsersByCategory(UUID categoryId) {
    try {
      List<User> users = userRepository.findByCategoryId(categoryId);
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error retrieving users by category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve users by category", ex);
    }
  }

  public List<UserResponse> getUsersByCategoryLabel(String categoryLabel) {
    try {
      List<User> users = userRepository.findByCategoryLabel(categoryLabel);
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error retrieving users by category label: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve users by category label", ex);
    }
  }

  public List<UserResponse> getRecentUsers(int limit) {
    try {
      List<User> users = userRepository.findRecentUsers(limit);
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error retrieving recent users: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve recent users", ex);
    }
  }

  public List<UserResponse> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
    try {
      List<User> users = userRepository.findByCreatedAtBetween(startDate, endDate);
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error retrieving users created between dates: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve users created between dates", ex);
    }
  }

  @Transactional
  public UserResponse createUser(UserCreationRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResourceAlreadyExistsException(
          "User already exists with email: " + request.getEmail());
    }

    try {
      UserCategory category = categoryService.getCategoryEntityById(request.getCategoryId());
      User user = userMapper.toEntity(request, category);
      User saved = userRepository.save(user);

      log.info("User created successfully: {} ({})", saved.getEmail(), saved.getId());

      auditLogService.logAction(
          "USER_CREATED", "User", saved.getId(), "User " + saved.getEmail() + " created by admin");

      return userMapper.toResponse(saved);
    } catch (Exception ex) {
      log.error("Error creating user: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create user", ex);
    }
  }

  @Transactional
  public User createUserEntity(User user) {
    try {
      if (userRepository.existsByEmail(user.getEmail())) {
        throw new ResourceAlreadyExistsException(
            "User already exists with email: " + user.getEmail());
      }
      User saved = userRepository.save(user);
      log.info("User entity created: {}", saved.getEmail());
      return saved;
    } catch (Exception ex) {
      log.error("Error creating user entity: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create user entity", ex);
    }
  }

  @Transactional
  public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
    User user = getUserEntityById(userId);

    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new ResourceAlreadyExistsException("Email already in use: " + request.getEmail());
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

      auditLogService.logAction(
          "USER_UPDATED", "User", updated.getId(), "User " + updated.getEmail() + " updated");

      return userMapper.toResponse(updated);
    } catch (Exception ex) {
      log.error("Error updating user: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update user", ex);
    }
  }

  @Transactional
  public User updateUserEntity(User user) {
    try {
      User updated = userRepository.save(user);
      log.info("User entity updated: {}", updated.getEmail());
      return updated;
    } catch (Exception ex) {
      log.error("Error updating user entity: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to update user entity", ex);
    }
  }

  @Transactional
  public void updateLastLogin(UUID userId) {
    try {
      User user = getUserEntityById(userId);
      user.setLastLogin(LocalDateTime.now());
      userRepository.save(user);
    } catch (Exception ex) {
      log.error(" Error updating last login for user {}: {}", userId, ex.getMessage());
      throw new DatabaseException("Failed to update last login", ex);
    }
  }

  @Transactional
  public User updateLastLogin(String email) {
    try {
      User user = getUserEntityByEmail(email);
      user.setLastLogin(LocalDateTime.now());
      return userRepository.save(user);
    } catch (Exception ex) {
      log.error("Error updating last login for user {}: {}", email, ex.getMessage());
      throw new DatabaseException("Failed to update last login", ex);
    }
  }

  @Transactional
  public void changePassword(UUID userId, String currentPassword, String newPassword) {
    User user = getUserEntityById(userId);

    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new InvalidCredentialsException("Current password is incorrect");
    }

    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
      throw new ValidationException("New password must be different from current password");
    }

    try {
      user.setPasswordHash(passwordEncoder.encode(newPassword));
      userRepository.save(user);
      log.info("Password  changed for user: {}", userId);

      auditLogService.logAction(
          "PASSWORD_CHANGED", "User", userId, "Password changed for user " + user.getEmail());
    } catch (Exception ex) {
      log.error("Error changing password: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to change password", ex);
    }
  }

  @Transactional
  public void changePassword(UUID userId, String newPassword) {
    User user = getUserEntityById(userId);

    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
      throw new ValidationException("New password must be different from current password");
    }

    try {
      user.setPasswordHash(passwordEncoder.encode(newPassword));
      userRepository.save(user);
      log.info("Password changed for user: {}", userId);

      auditLogService.logAction(
          "PASSWORD_CHANGED",
          "User",
          userId,
          "Password changed for user " + user.getEmail() + " by admin");
    } catch (Exception ex) {
      log.error("Error changing password: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to change password", ex);
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
      log.info("User deactivated: {} ({})", user.getEmail(), userId);

      auditLogService.logAction(
          "USER_DEACTIVATED", "User", userId, "User " + user.getEmail() + " deactivated");
    } catch (Exception ex) {
      log.error("Error deactivating user: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to deactivate user", ex);
    }
  }

  @Transactional
  public UserResponse activateUser(UUID userId) {
    User user = getUserEntityById(userId);

    if (user.getIsActive()) {
      throw new ValidationException("User is already active");
    }

    try {
      user.setIsActive(true);
      User updated = userRepository.save(user);
      log.info("User activated: {} ({})", user.getEmail(), userId);

      auditLogService.logAction(
          "USER_ACTIVATED", "User", userId, "User " + user.getEmail() + " activated");

      return userMapper.toResponse(updated);
    } catch (Exception ex) {
      log.error("Error activating user: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to activate user", ex);
    }
  }

  @Transactional
  public void deleteUser(UUID userId) {
    User user = getUserEntityById(userId);

    try {
      userRepository.delete(user);
      log.info("User deleted: {} ({})", user.getEmail(), userId);

      auditLogService.logAction(
          "USER_DELETED", "User", userId, "User " + user.getEmail() + " deleted");
    } catch (Exception ex) {
      log.error("Error deleting user: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete user", ex);
    }
  }

  @Transactional
  public void hardDeleteUser(UUID userId) {
    deleteUser(userId);
  }

  public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
    try {
      Page<User> users = userRepository.searchUsers(searchTerm, pageable);
      return users.map(userMapper::toResponse);
    } catch (Exception ex) {
      log.error("Error searching users: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to search users", ex);
    }
  }

  public List<UserResponse> searchUsers(String searchTerm) {
    try {
      List<User> users = userRepository.searchUsers(searchTerm);
      return users.stream().map(userMapper::toResponse).toList();
    } catch (Exception ex) {
      log.error("Error searching users: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to search users", ex);
    }
  }

  public long countUsers() {
    return userRepository.count();
  }

  public long countActiveUsers() {
    return userRepository.countByIsActiveTrue();
  }

  public long countInactiveUsers() {
    return userRepository.countByIsActiveFalse();
  }

  public long countUsersByCategory(UUID categoryId) {
    return userRepository.countByCategoryId(categoryId);
  }

  public long countUsersByCategoryLabel(String categoryLabel) {
    return userRepository.countByCategoryLabel(categoryLabel);
  }

  public long countUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return userRepository.countByCreatedAtBetween(startDate, endDate);
  }

  public long countUsersCreatedAfter(LocalDateTime date) {
    return userRepository.countByCreatedAtAfter(date);
  }

  public long countUsersCreatedBefore(LocalDateTime date) {
    return userRepository.countByCreatedAtBefore(date);
  }

  public UserStatisticsResponse getUserStatistics() {
    try {
      return customUserRepository.getUserStatistics();
    } catch (Exception ex) {
      log.error("Error retrieving user statistics: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve user statistics", ex);
    }
  }

  public UserStatisticsResponse getUserStatisticsByCategory(UUID categoryId) {
    try {
      return customUserRepository.getUserStatisticsByCategoryId(categoryId);
    } catch (Exception ex) {
      log.error("Error retrieving user statistics by category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to retrieve user statistics by category", ex);
    }
  }

  public List<Object[]> getUsersGroupedByCategory() {
    try {
      return customUserRepository.countGroupByCategory();
    } catch (Exception ex) {
      log.error("Error getting users grouped by category: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get users grouped by category", ex);
    }
  }

  public List<Object[]> getUsersGroupedByMonth(int year) {
    try {
      return customUserRepository.countByMonth(year);
    } catch (Exception ex) {
      log.error("Error getting users grouped by month: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get users grouped by month", ex);
    }
  }

  public List<Object[]> getUsersGroupedByMonth() {
    try {
      return customUserRepository.countByMonth();
    } catch (Exception ex) {
      log.error("Error getting users grouped by month: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get users grouped by month", ex);
    }
  }

  public List<Object[]> getUsersGroupedByYear(int year) {
    try {
      return customUserRepository.countByYear();
    } catch (Exception ex) {
      log.error("Error getting users grouped by year: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to get users grouped by year", ex);
    }
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean existsActiveByEmail(String email) {
    return userRepository.existsByEmailAndIsActiveTrue(email);
  }

  public boolean existsById(UUID userId) {
    return userRepository.existsById(userId);
  }

  public boolean isUserActive(UUID userId) {
    User user = getUserEntityById(userId);
    return user.getIsActive();
  }

  @Transactional
  public void deactivateUsersBulk(List<UUID> userIds) {
    try {
      for (UUID userId : userIds) {
        deactivateUser(userId);
      }
      log.info("Deactivated {} users in bulk", userIds.size());
    } catch (Exception ex) {
      log.error("Error deactivating users in bulk: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to deactivate users in bulk", ex);
    }
  }

  @Transactional
  public void activateUsersBulk(List<UUID> userIds) {
    try {
      for (UUID userId : userIds) {
        activateUser(userId);
      }
      log.info("Activated {} users in bulk", userIds.size());
    } catch (Exception ex) {
      log.error("Error activating users in bulk: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to activate users in bulk", ex);
    }
  }

  @Transactional
  public void deleteUsersBulk(List<UUID> userIds) {
    try {
      for (UUID userId : userIds) {
        deleteUser(userId);
      }
      log.info("Deleted {} users in bulk", userIds.size());
    } catch (Exception ex) {
      log.error("Error deleting users in bulk: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to delete users in bulk", ex);
    }
  }

  @Transactional
  public UserResponse createAdminUser(UserCreationRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResourceAlreadyExistsException(
          "User already exists with email: " + request.getEmail());
    }

    try {
      UserCategory adminCategory = categoryService.getCategoryEntityByLabel("ADMIN");
      User user = userMapper.toEntity(request, adminCategory);
      User saved = userRepository.save(user);

      log.info("Admin user created: {}", saved.getEmail());

      auditLogService.logAction(
          "ADMIN_USER_CREATED",
          "User",
          saved.getId(),
          "Admin user " + saved.getEmail() + " created");

      return userMapper.toResponse(saved);
    } catch (Exception ex) {
      log.error("Error creating admin user: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to create admin user", ex);
    }
  }

  @Transactional
  public void resetPasswordByAdmin(UUID userId, String newPassword) {
    User user = getUserEntityById(userId);

    try {
      user.setPasswordHash(passwordEncoder.encode(newPassword));
      userRepository.save(user);
      log.info("Password reset by admin for user: {}", userId);

      auditLogService.logAction(
          "PASSWORD_RESET_BY_ADMIN",
          "User",
          userId,
          "Password reset by admin for user " + user.getEmail());
    } catch (Exception ex) {
      log.error("Error resetting password: {}", ex.getMessage(), ex);
      throw new DatabaseException("Failed to reset password", ex);
    }
  }

  // ==================== ROLE METHODS ====================

  public UserResponse changeUserRole(UUID userId, UserRole newRole) {
    User user = getUserEntityById(userId);
    user.setRole(newRole);
    User updated = userRepository.save(user);
    log.info("User role changed: {} -> {} for user: {}", user.getEmail(), newRole, userId);

    auditLogService.logAction(
            "USER_ROLE_CHANGED",
            "User",
            userId,
            "User " + user.getEmail() + " role changed to " + newRole.getDisplayName()
    );

    return userMapper.toResponse(updated);
  }

  public Page<UserResponse> getUsersByRole(UserRole role, Pageable pageable) {
    Page<User> users = userRepository.findByRole(role, pageable);
    return users.map(userMapper::toResponse);
  }

  public long countUsersByRole(UserRole role) {
    return userRepository.countByRole(role);
  }

  public List<User> getUsersByRole(UserRole role) {
    return userRepository.findByRole(role);
  }

  public List<User> getUsersWithRoleAndActive(UserRole role, boolean active) {
    return userRepository.findByRoleAndIsActive(role, active);
  }

  public String getCurrentUserEmail() {
    return null;
  }

  public User getCurrentUser() {
    return null;
  }

  public UUID getCurrentUserId() {
    return null;
  }
}
