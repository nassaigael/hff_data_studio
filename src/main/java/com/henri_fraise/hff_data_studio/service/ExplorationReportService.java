package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.ExplorationReportMapper;
import com.henri_fraise.hff_data_studio.repository.ExplorationReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplorationReportService {

	private final ExplorationReportRepository reportRepository;
	private final ExplorationReportMapper reportMapper;
	private final DatasetService datasetService;
	private final AuditLogService auditLogService;

	// ==================== CRUD Operations ====================

	public ExplorationReportResponse getReportByDatasetId(UUID datasetId) {
		ExplorationReport report = getReportEntityByDatasetId(datasetId);
		return reportMapper.toResponse(report);
	}

	public ExplorationReport getReportEntityByDatasetId(UUID datasetId) {
		return reportRepository.findByDatasetId(datasetId)
				.orElseThrow(() -> new ResourceNotFoundException("ExplorationReport", "dataset", datasetId));
	}

	@Transactional
	public ExplorationReportResponse generateReport(UUID datasetId) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);

		try {
			// In production, this would call Python service to analyze data
			// For now, we'll create a placeholder report
			ExplorationReport report = ExplorationReport.builder()
					.dataset(dataset)
					.totalRows(dataset.getRowCount())
					.duplicateCount(0)
					.missingValuesCount(0)
					.qualityScore(BigDecimal.valueOf(85.5))
					.build();

			ExplorationReport saved = reportRepository.save(report);

			log.info("Exploration report generated for dataset: {} ({})",
					dataset.getDatasetName(), datasetId);

			// Audit log
			auditLogService.logAction(
					"REPORT_GENERATED",
					"ExplorationReport",
					saved.getId(),
					"Exploration report generated for dataset " + dataset.getDatasetName()
			);

			return reportMapper.toResponse(saved);
		} catch (Exception ex) {
			log.error("Error generating exploration report: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to generate exploration report", ex);
		}
	}

	@Transactional
	public ExplorationReportResponse updateQualityScore(UUID datasetId, BigDecimal score) {
		ExplorationReport report = getReportEntityByDatasetId(datasetId);

		try {
			report.setQualityScore(score);
			ExplorationReport updated = reportRepository.save(report);

			log.info("Quality score updated for dataset {}: {}", datasetId, score);
			return reportMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating quality score: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update quality score", ex);
		}
	}

	@Transactional
	public void updateReportPdfPath(UUID reportId, String pdfPath) {
		try {
			reportRepository.updateReportPdfPath(reportId, pdfPath);
			log.info("Report PDF path updated: {} -> {}", reportId, pdfPath);
		} catch (Exception ex) {
			log.error("Error updating report PDF path: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update report PDF path", ex);
		}
	}

	// ==================== Statistics Operations ====================

	public double getAverageQualityScore() {
		try {
			BigDecimal avg = reportRepository.averageQualityScore();
			return avg != null ? avg.doubleValue() : 0.0;
		} catch (Exception ex) {
			log.error("Error getting average quality score: {}", ex.getMessage(), ex);
			return 0.0;
		}
	}

	public long countReportsWithQualityScoreAbove(BigDecimal threshold) {
		return reportRepository.countByQualityScoreGreaterThanEqual(threshold);
	}

	public long countReportsWithQualityScoreBelow(BigDecimal threshold) {
		return reportRepository.countByQualityScoreLessThan(threshold);
	}

	public Double getAverageQualityScoreForDataset(UUID datasetId) {
		ExplorationReport report = reportRepository.findByDatasetId(datasetId)
					.orElseThrow(() -> new ResourceNotFoundException("ExplorationReport", "dataset", datasetId));
			return report.getQualityScore().doubleValue();
	}
}