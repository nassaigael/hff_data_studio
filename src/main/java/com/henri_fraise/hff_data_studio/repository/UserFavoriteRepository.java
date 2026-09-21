package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.UserFavorite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {

	List<UserFavorite> findByUserIdOrderByCreatedAtDesc(UUID userId);

	Page<UserFavorite> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

	Optional<UserFavorite> findByUserIdAndEntityTypeAndEntityId(
			UUID userId, String entityType, UUID entityId);

	boolean existsByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);

	void deleteByUserIdAndEntityTypeAndEntityId(UUID userId, String entityType, UUID entityId);

	long countByUserId(UUID userId);
}