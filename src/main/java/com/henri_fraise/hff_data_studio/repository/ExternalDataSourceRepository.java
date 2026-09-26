package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ExternalDataSource;
import com.henri_fraise.hff_data_studio.enums.DataSourceType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalDataSourceRepository extends JpaRepository<ExternalDataSource, UUID> {

	List<ExternalDataSource> findByUserIdOrderByCreatedAtDesc(UUID userId);

	List<ExternalDataSource> findByType(DataSourceType type);
}