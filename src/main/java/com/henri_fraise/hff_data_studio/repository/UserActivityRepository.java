package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.UserActivity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {

	Page<UserActivity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

	List<UserActivity> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

	@Modifying
	@Query("DELETE FROM UserActivity a WHERE a.createdAt < :threshold")
	int deleteOldActivities(@Param("threshold") LocalDateTime threshold);
}