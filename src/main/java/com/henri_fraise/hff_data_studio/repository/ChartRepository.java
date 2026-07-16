package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.ChartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChartRepository  extends JpaRepository<Chart, UUID> {
	
	Optional<Chart> findByResultId(UUID resultId);

	List<Chart> findByChartType(ChartType chartType);

	long countByChartType(ChartType chartType);
	
}
