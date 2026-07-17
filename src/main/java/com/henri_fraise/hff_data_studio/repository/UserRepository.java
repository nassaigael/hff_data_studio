package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  Page<User> findAllByIsActiveTrue(Pageable pageable);

  List<User> findByIsActiveTrue();

  List<User> findByIsActiveFalse();

  Page<User> findByCategoryId(UUID categoryId, Pageable pageable);

  List<User> findByCategoryId(UUID categoryId);

  List<User> findByCategoryLabel(String categoryLabel);

  List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  List<User> findByCreatedAtBefore(LocalDateTime date);

  List<User> findByCreatedAtAfter(LocalDateTime date);

  long countByIsActiveTrue();

  long countByIsActiveFalse();

  long countByCategoryId(UUID categoryId);

  long countByCategoryLabel(String categoryLabel);

  long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  long countByCreatedAtAfter(LocalDateTime date);

  long countByCreatedAtBefore(LocalDateTime date);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIsActiveTrue(String email);

  @Query("SELECT u FROM User u WHERE " +
          "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
          "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
          "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

  @Query("SELECT u FROM User u WHERE " +
          "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
          "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
          "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<User> searchUsers(@Param("searchTerm") String searchTerm);

  @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
  List<User> findRecentUsers(@Param("limit") int limit);

  @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.lastLogin DESC")
  List<User> findRecentlyActiveUsers(@Param("limit") int limit);
}