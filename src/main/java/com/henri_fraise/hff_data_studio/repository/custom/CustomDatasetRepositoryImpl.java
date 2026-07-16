package com.henri_fraise.hff_data_studio.repository.custom;

import com.henri_fraise.hff_data_studio.dto.response.DatasetStatisticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CustomDatasetRepositoryImpl implements CustomDatasetRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public DatasetStatisticsResponse getDatasetStatistics() {
		Query query = entityManager.createNativeQuery(
				"SELECT " +
						"COUNT(*) as total_datasets, " +
						"SUM(row_count) as total_rows, " +
						"SUM(column_count) as total_columns, " +
						"SUM(CASE WHEN is_cleaned = true THEN 1 ELSE 0 END) as cleaned_datasets, " +
						"SUM(CASE WHEN is_cleaned = false THEN 1 ELSE 0 END) as uncleaned_datasets, " +
						"AVG(er.quality_score) as avg_quality_score, " +
						"(SELECT COUNT(*) FROM dataset WHERE created_at >= NOW() - INTERVAL '30 days') as new_datasets_last_30_days " +
						"FROM dataset d " +
						"LEFT JOIN exploration_report er ON d.dataset_id = er.dataset_id"
		);

		Object[] result = (Object[]) query.getSingleResult();

		return DatasetStatisticsResponse.builder()
				.totalDatasets(((Number) result[0]).longValue())
				.totalRows(((Number) result[1]).longValue())
				.totalColumns(((Number) result[2]).longValue())
				.cleanedDatasets(((Number) result[3]).longValue())
				.uncleanedDatasets(((Number) result[4]).longValue())
				.averageQualityScore(result[5] != null ? ((BigDecimal) result[5]).doubleValue() : 0.0)
				.newDatasetsLast30Days(((Number) result[6]).longValue())
				.build();
	}

	@Override
	public long countDatasetsByProjectId(UUID projectId) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId"
		);
		query.setParameter("projectId", projectId);
		return ((Number) query.getSingleResult()).longValue();
	}

	@Override
	public long countCleanedDatasetsByProjectId(UUID projectId) {
		Query query = entityManager.createQuery(
				"SELECT COUNT(d) FROM Dataset d WHERE d.sourceFile.project.id = :projectId AND d.isCleaned = true"
		);
		query.setParameter("projectId", projectId);
		return ((Number) query.getSingleResult()).longValue();
	}

	@Override
	public double getAverageDatasetQualityScore() {
		Query query = entityManager.createQuery(
				"SELECT AVG(r.qualityScore) FROM ExplorationReport r"
		);
		BigDecimal result = (BigDecimal) query.getSingleResult();
		return result != null ? result.doubleValue() : 0.0;
	}

	@Override
	@Transactional
	public void updateDatasetQualityScore(UUID datasetId, double score) {
		Query query = entityManager.createNativeQuery(
				"UPDATE exploration_report SET quality_score = :score WHERE dataset_id = :datasetId"
		);
		query.setParameter("score", score);
		query.setParameter("datasetId", datasetId);
		query.executeUpdate();
	}
}