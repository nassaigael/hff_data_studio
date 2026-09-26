package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Notification;
import com.henri_fraise.hff_data_studio.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

	Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

	long countByUserIdAndIsReadFalse(UUID userId);

	long countByUserId(UUID userId);

	Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type, Pageable pageable);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now " +
			"WHERE n.user.id = :userId AND n.isRead = false")
	int markAllAsRead(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

	@Modifying
	@Query("DELETE FROM Notification n WHERE n.user.id = :userId")
	void deleteByUserId(@Param("userId") UUID userId);

	@Modifying
	@Query("DELETE FROM Notification n WHERE n.createdAt < :threshold")
	int deleteOldNotifications(@Param("threshold") LocalDateTime threshold);
}