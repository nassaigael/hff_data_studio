package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DatasetColumnRepository extends JpaRepository<DatasetColumn, UUID> {

	List<DatasetColumn> findByDatasetIdOrderByPositionAsc(UUID datasetId);

	List<DatasetColumn> findByDatasetId(UUID datasetId);

	Optional<DatasetColumn> findByDatasetIdAndOriginalName(UUID datasetId, String originalName);

	List<DatasetColumn> findByDatasetIdAndDetectedType(UUID datasetId, ColumnType detectedType);

	List<DatasetColumn> findByDatasetIdAndNormalizedNameIsNotNull(UUID datasetId);

	boolean existsByDatasetId(UUID datasetId);

	long countByDatasetId(UUID datasetId);

	long countByDatasetIdAndNullCountGreaterThan(UUID datasetId, Integer nullCount);

	@Query("SELECT SUM(c.uniqueCount) FROM DatasetColumn c WHERE c.dataset.id = :dataset_id")
	Long sumUniqueCountByDatasetId(@Param("dataset_id") UUID datasetId);

	@Query("SELECT SUM(c.nullCount) FROM DatasetColumn  c WHERE c.dataset.id = :dataset_id")
	Long sumNullCountByDatasetId(@Param("dataset_id") UUID datasetId);

	@Modifying
	@Transactional
	@Query("UPDATE DatasetColumn c SET c.normalizedName = :normalized_name WHERE c.id = :column_id")
	void updateNormalizedName(
			@Param("column_id") UUID columnId, @Param("normalized_name") String normalizedName);

	@Modifying
	@Transactional
	@Query("UPDATE DatasetColumn c SET c.targetType = :target_type WHERE c.id = :column_id")
	void updateTargetType(
			@Param("column_id") UUID columnId, @Param("target_type") ColumnType targetType);

	@Modifying
	@Transactional
	@Query(
			"UPDATE DatasetColumn c SET c.nullCount = :null_count, c.uniqueCount = :unique_count WHERE"
					+ " c.id = :column_id")
	void updateStats(
			@Param("column_id") UUID columnId,
			@Param("null_count") Integer nullCount,
			@Param("unique_count") Integer uniqueCount);

	@Query(
			"SELECT c FROM DatasetColumn  c WHERE  c.dataset.id = :dataset_id AND "
					+ "(c.normalizedName IS NULL OR c.targetType IS NULL OR c.targetType != c.detectedType)")
	List<DatasetColumn> findColumnsNeedingNormalization(@Param("dataset_id") UUID datasetId);

	@Query("SELECT c FROM DatasetColumn c WHERE c.dataset.id = :dataset_id AND c.nullCount > 0")
	List<DatasetColumn> findColumnsWithNullsValues(@Param("dataset_id") UUID datasetId);

	@Query(
			"SELECT c FROM DatasetColumn c WHERE c.dataset.id = :dataset_id AND c.uniqueCount ="
					+ " c.dataset.rowCount")
	List<DatasetColumn> findUniqueColumns(@Param("dataset_id") UUID datasetId);

	@Query("SELECT MAX(c.position) FROM DatasetColumn c WHERE c.dataset.id = :dataset_id")
	Integer findMaxPositionByDatasetId(@Param("dataset_id") UUID datasetId);

	@Query(
			"SELECT c FROM DatasetColumn c WHERE c.dataset.id = :dataset_id AND c.detectedType ="
					+ " :detected_type AND c.targetType IS NULL ")
	List<DatasetColumn> findColumnsWithDetectedTypeNotConverted(
			@Param("dataset_id") UUID datasetId, @Param("detected_type") ColumnType detectedType);

	Optional<DatasetColumn> findByIdAndDatasetId(UUID id, UUID datasetId);
}
