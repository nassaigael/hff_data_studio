package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailIsAndIsActiveTrue(String email);

  Page<User> findAllByIsActiveTrue(Pageable pageable);

  List<User> findAllByIsActiveTrue();

  Page<User> findByIsActiveTrueAndLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(
      String lastName, String firstName, Pageable pageable);

  Page<User> findByCategory_Id(UUID categoryId, Pageable pageable);

  List<User> findByLastLoginBefore(LocalDateTime date);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIsActiveTrue(String email);

  boolean existsByEmailAndIdNot(String email, UUID id);

  long countByIsActiveTrue();

  long countByCategory_Id(UUID categoryId);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.isActive = false WHERE u.id = :user_id")
  void deactivateUser(@Param("user_id") UUID userId);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :user_id")
  void updateLastLogin(@Param("user_id") UUID userId, @Param("lastLogin") LocalDateTime lastLogin);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :user_id")
  void updatePassword(@Param("user_id") UUID userId, @Param("passwordHash") String passwordHash);

  @Query(
      "SELECT u FROM User u WHERE u.isActive = true AND "
          + "(:searchTerm IS NULL OR "
          + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

  @Query("SELECT u FROM User u WHERE u.isActive = true AND u.category.label = :categoryLabel")
  List<User> findActiveUsersByCategoryLabel(@Param("categoryLabel") String categoryLabel);

  @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
  long countUsersCreatedBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
