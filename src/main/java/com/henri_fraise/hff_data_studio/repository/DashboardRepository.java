package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Dashboard;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

	List<Dashboard> findByUserIdOrderByCreatedAtDesc(UUID userId);

	List<Dashboard> findByProjectId(UUID projectId);

	List<Dashboard> findByIsPublicTrue();

	boolean existsByUserIdAndName(UUID userId, String name);
}