package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.DashboardWidget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, UUID> {

	List<DashboardWidget> findByDashboardIdOrderByPositionAsc(UUID dashboardId);

	void deleteByDashboardId(UUID dashboardId);
}