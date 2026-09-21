package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.DatasetVersion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, UUID> {

	List<DatasetVersion> findByDatasetIdOrderByVersionNumberDesc(UUID datasetId);

	Optional<DatasetVersion> findByDatasetIdAndVersionNumber(UUID datasetId, Integer versionNumber);

	Optional<DatasetVersion> findByDatasetIdAndIsCurrentTrue(UUID datasetId);

	long countByDatasetId(UUID datasetId);
}