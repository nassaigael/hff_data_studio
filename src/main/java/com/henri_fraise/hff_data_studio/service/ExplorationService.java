package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplorationService {

	private final ExplorationReportRepository reportRepository;
	private final ExplorationReportMapper reportMapper;
	private final DatasetService datasetService;
	private final DatasetColumnService columnService;
	private final AuditLogService auditLogService;

	public ExplorationReport getReportEntityById(UUID reportId) {
		return reportRepository.findById(reportId)
				.orElseThrow(() -> new ResourceNotFoundException("Exploration report not found with id: " + reportId));
	}

	public ExplorationReport getReportByDatasetId(UUID datasetId) {
		return reportRepository.findByDatasetId(datasetId)
				.orElse(null);
	}

	@Transactional
	public ExplorationReportResponse getExplorationReport(UUID datasetId) {
		try {
			ExplorationReportResponse report = reportMapper.toResponse(getReportByDatasetId(datasetId));
			if (report == null) {
				report = generateExplorationReport(datasetId);
			}
			return report;
		} catch (Exception ex) {
			log.error("Error getting exploration report: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get exploration report", ex);
		}
	}

	public ExplorationReportResponse getExplorationReportById(UUID reportId) {
		ExplorationReport report = getReportEntityById(reportId);
		return reportMapper.toResponse(report);
	}


	@Transactional
	public ExplorationReportResponse getExplorationSummary(UUID datasetId) {
		try {
			ExplorationReportResponse report = reportMapper.toResponse(getReportByDatasetId(datasetId));
			if (report == null) {
				report = generateExplorationReport(datasetId);
			}
			return report;
		} catch (Exception ex) {
			log.error("Error getting exploration summary: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get exploration summary", ex);
		}
	}

	@Transactional
	public ExplorationReportResponse generateExplorationReport(UUID datasetId) {
		try {
			Dataset dataset = datasetService.getDatasetEntityById(datasetId);
			List<DatasetColumn> columns = columnService.getColumnsByDataset(datasetId);

			int totalRows = dataset.getRowCount();
			int duplicateCount = calculateDuplicates(dataset);
			int missingValuesCount = calculateMissingValues(columns);
			BigDecimal qualityScore = calculateQualityScore(totalRows, duplicateCount, missingValuesCount, columns);

			ExplorationReport report = ExplorationReport.builder()
					.dataset(dataset)
					.totalRows(totalRows)
					.duplicateCount(duplicateCount)
					.missingValuesCount(missingValuesCount)
					.qualityScore(qualityScore)
					.generatedAt(LocalDateTime.now())
					.build();

			ExplorationReport saved = reportRepository.save(report);
			log.info("Exploration report generated for dataset: {}", datasetId);

			auditLogService.logAction(
					"EXPLORATION_REPORT_GENERATED",
					"Dataset",
					datasetId,
					"Exploration report generated for dataset: " + dataset.getDatasetName()
			);

			return reportMapper.toResponse(saved);

		} catch (Exception ex) {
			log.error("Error generating exploration report: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to generate exploration report", ex);
		}
	}

	@Transactional
	public ExplorationReportResponse regenerateExplorationReport(UUID datasetId) {
		try {
			ExplorationReport existing = getReportByDatasetId(datasetId);
			if (existing != null) {
				reportRepository.delete(existing);
				log.info("Deleted existing exploration report for dataset: {}", datasetId);
			}
			return generateExplorationReport(datasetId);
		} catch (Exception ex) {
			log.error("Error regenerating exploration report: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to regenerate exploration report", ex);
		}
	}

	private int calculateDuplicates(Dataset dataset) {
		try {
			return 0;
		} catch (Exception ex) {
			log.warn("Error calculating duplicates: {}", ex.getMessage());
			return 0;
		}
	}

	private int calculateMissingValues(List<DatasetColumn> columns) {
		try {
			return columns.stream()
					.mapToInt(DatasetColumn::getNullCount)
					.sum();
		} catch (Exception ex) {
			log.warn("Error calculating missing values: {}", ex.getMessage());
			return 0;
		}
	}

	private BigDecimal calculateQualityScore(int totalRows, int duplicateCount, int missingValuesCount,
	                                         List<DatasetColumn> columns) {
		try {
			int totalCells = totalRows * columns.size();
			if (totalCells == 0) {
				return BigDecimal.ZERO;
			}

			double missingRatio = (double) missingValuesCount / totalCells;
			double duplicateRatio = totalRows > 0 ? (double) duplicateCount / totalRows : 0;

			double score = (1 - missingRatio - (duplicateRatio * 0.5)) * 100;
			score = Math.clamp(score, 0, 100);

			return BigDecimal.valueOf(score)
					.setScale(2, RoundingMode.HALF_UP);
		} catch (Exception ex) {
			log.warn("Error calculating quality score: {}", ex.getMessage());
			return BigDecimal.ZERO;
		}
	}

	public Map<String, Object> getColumnStatistics(UUID datasetId) {
		try {
			List<DatasetColumn> columns = columnService.getColumnsByDataset(datasetId);
			Dataset dataset = datasetService.getDatasetEntityById(datasetId);

			Map<String, Object> stats = new HashMap<>();
			stats.put("datasetId", datasetId);
			stats.put("datasetName", dataset.getDatasetName());
			stats.put("totalColumns", columns.size());
			stats.put("totalRows", dataset.getRowCount());

			Map<String, Object> columnStats = new HashMap<>();
			for (DatasetColumn column : columns) {
				Map<String, Object> colStats = new HashMap<>();
				get(column, colStats);

				if (dataset.getRowCount() > 0) {
					double nullPercentage = (double) column.getNullCount() / dataset.getRowCount() * 100;
					double uniquePercentage = (double) column.getUniqueCount() / dataset.getRowCount() * 100;
					colStats.put("nullPercentage", BigDecimal.valueOf(nullPercentage)
							.setScale(2, RoundingMode.HALF_UP));
					colStats.put("uniquePercentage", BigDecimal.valueOf(uniquePercentage)
							.setScale(2, RoundingMode.HALF_UP));
				}

				columnStats.put(column.getId().toString(), colStats);
			}

			stats.put("columns", columnStats);
			return stats;

		} catch (Exception ex) {
			log.error("Error getting column statistics: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get column statistics", ex);
		}
	}

	static void get(DatasetColumn column, Map<String, Object> colStats) {
		colStats.put("originalName", column.getOriginalName());
		colStats.put("normalizedName", column.getNormalizedName());
		colStats.put("detectedType", column.getDetectedType());
		colStats.put("targetType", column.getTargetType());
		colStats.put("nullCount", column.getNullCount());
		colStats.put("uniqueCount", column.getUniqueCount());
	}

	public Map<String, Object> getQualityScoreDistribution() {
		try {
			Map<String, Object> distribution = new HashMap<>();
			List<Object[]> results = reportRepository.getQualityScoreDistribution();

			for (Object[] result : results) {
				String range = (String) result[0];
				Long count = (Long) result[1];
				distribution.put(range, count);
			}

			return distribution;
		} catch (Exception ex) {
			log.error("Error getting quality score distribution: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get quality score distribution", ex);
		}
	}

	@Transactional
	public byte[] exportExplorationReport(UUID datasetId, String format) {
		try {
			ExplorationReport report = getReportByDatasetId(datasetId);
			if (report == null) {
				report = reportMapper.toEntity(generateExplorationReport(datasetId));
			}

			if ("PDF".equalsIgnoreCase(format)) {
				return generatePdfReport(report);
			} else if ("HTML".equalsIgnoreCase(format)) {
				return generateHtmlReport(report);
			} else {
				throw new IllegalArgumentException("Unsupported format: " + format);
			}
		} catch (Exception ex) {
			log.error("Error exporting exploration report: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to export exploration report", ex);
		}
	}

	private byte[] generatePdfReport(ExplorationReport report) {
		try {
			String pdfContent = "=== EXPLORATION REPORT ===\n" +
					"Generated: " + report.getGeneratedAt() + "\n" +
					"Dataset: " + report.getDataset().getDatasetName() + "\n" +
					"Total Rows: " + report.getTotalRows() + "\n" +
					"Duplicate Count: " + report.getDuplicateCount() + "\n" +
					"Missing Values: " + report.getMissingValuesCount() + "\n" +
					"Quality Score: " + report.getQualityScore() + "/100\n";
			return pdfContent.getBytes();
		} catch (Exception ex) {
			log.error("Error generating PDF report: {}", ex.getMessage(), ex);
			return "Error generating PDF report".getBytes();
		}
	}

	private byte[] generateHtmlReport(ExplorationReport report) {
		try {
			String html = "<!DOCTYPE html><html><head><title>Exploration Report</title>" +
					"<style>body{font-family:Arial;margin:40px;background:#f5f5f5;}" +
					".container{background:white;padding:30px;border-radius:10px;box-shadow:0 2px 10px rgba(0,0,0,0.1);}" +
					"h1{color:#0B2545;border-bottom:3px solid #FFCD11;padding-bottom:10px;}" +
					".score{font-size:48px;font-weight:bold;color:#0B2545;}" +
					".highlight{background:#FFCD11;padding:5px 15px;border-radius:5px;display:inline-block;}" +
					"table{width:100%;border-collapse:collapse;margin-top:20px;}" +
					"th,td{padding:12px;text-align:left;border-bottom:1px solid #ddd;}" +
					"th{background:#0B2545;color:white;}" +
					"</style></head><body>" +
					"<div class='container'>" +
					"<h1>📊 Exploration Report</h1>" +
					"<p><strong>Generated:</strong> " + report.getGeneratedAt() + "</p>" +
					"<p><strong>Dataset:</strong> " + report.getDataset().getDatasetName() + "</p>" +
					"<hr>" +
					"<h2>📈 Quality Score</h2>" +
					"<div class='score'>" + report.getQualityScore() + "<span style='font-size:20px;color:#666;'>/100</span></div>" +
					"<br>" +
					"<h2>📋 Summary Statistics</h2>" +
					"<table><tr><th>Metric</th><th>Value</th></tr>" +
					"<tr><td>Total Rows</td><td>" + report.getTotalRows() + "</td></tr>" +
					"<tr><td>Duplicate Count</td><td>" + report.getDuplicateCount() + "</td></tr>" +
					"<tr><td>Missing Values</td><td>" + report.getMissingValuesCount() + "</td></tr>" +
					"</table>" +
					"</div></body></html>";
			return html.getBytes();
		} catch (Exception ex) {
			log.error("Error generating HTML report: {}", ex.getMessage(), ex);
			return "Error generating HTML report".getBytes();
		}
	}

	public long countReports() {
		return reportRepository.count();
	}

	public long countReportsByDatasetId(UUID datasetId) {
		return reportRepository.countByDatasetId(datasetId);
	}

	public long countReportsWithQualityScoreAbove(BigDecimal threshold) {
		return reportRepository.countByQualityScoreGreaterThanEqual(threshold);
	}

	public long countReportsWithQualityScoreBelow(BigDecimal threshold) {
		return reportRepository.countByQualityScoreLessThanEqual(threshold);
	}

	public double getAverageQualityScore() {
		try {
			Double avg = reportRepository.averageQualityScore();
			return avg != null ? avg : 0.0;
		} catch (Exception ex) {
			log.error("Error getting average quality score: {}", ex.getMessage(), ex);
			return 0.0;
		}
	}

	public double getAverageQualityScoreForDataset(UUID datasetId) {
		try {
			Double avg = reportRepository.averageQualityScoreByDatasetId(datasetId);
			return avg != null ? avg : 0.0;
		} catch (Exception ex) {
			log.error("Error getting average quality score for dataset: {}", ex.getMessage(), ex);
			return 0.0;
		}
	}

	public List<Object[]> getTopQualityReports(int limit) {
		try {
			return reportRepository.findTopQualityReports(limit);
		} catch (Exception ex) {
			log.error("Error getting top quality reports: {}", ex.getMessage(), ex);
			return List.of();
		}
	}

	public List<Object[]> getBottomQualityReports(int limit) {
		try {
			return reportRepository.findBottomQualityReports(limit);
		} catch (Exception ex) {
			log.error("Error getting bottom quality reports: {}", ex.getMessage(), ex);
			return List.of();
		}
	}

	public Map<String, Object> getGlobalQualityStatistics() {
		try {
			Map<String, Object> stats = new HashMap<>();
			stats.put("totalReports", countReports());
			stats.put("averageQualityScore", getAverageQualityScore());
			stats.put("distribution", getQualityScoreDistribution());
			stats.put("topQualityReports", getTopQualityReports(5));
			stats.put("bottomQualityReports", getBottomQualityReports(5));
			return stats;
		} catch (Exception ex) {
			log.error("Error getting global quality statistics: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to get global quality statistics", ex);
		}
	}

	@Transactional
	public void deleteReport(UUID reportId) {
		ExplorationReport report = getReportEntityById(reportId);
		try {
			reportRepository.delete(report);
			log.info("Exploration report deleted: {}", reportId);

			auditLogService.logAction(
					"EXPLORATION_REPORT_DELETED",
					"ExplorationReport",
					reportId,
					"Exploration report deleted"
			);
		} catch (Exception ex) {
			log.error("Error deleting exploration report: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete exploration report", ex);
		}
	}

	@Transactional
	public void deleteReportByDatasetId(UUID datasetId) {
		ExplorationReport report = getReportByDatasetId(datasetId);
		if (report != null) {
			deleteReport(report.getId());
		}
	}

	public boolean existsByDatasetId(UUID datasetId) {
		return reportRepository.existsByDatasetId(datasetId);
	}

	public boolean hasQualityScoreAbove(UUID datasetId, BigDecimal threshold) {
		ExplorationReport report = getReportByDatasetId(datasetId);
		if (report == null) {
			return false;
		}
		return report.getQualityScore().compareTo(threshold) >= 0;
	}

	@Transactional
	public ExplorationReportResponse updateQualityScore(UUID reportId, BigDecimal qualityScore) {
		ExplorationReport report = getReportEntityById(reportId);
		try {
			report.setQualityScore(qualityScore);
			ExplorationReport updated = reportRepository.save(report);
			log.info("Quality score updated for report: {} -> {}", reportId, qualityScore);
			return reportMapper.toResponse(updated);
		} catch (Exception ex) {
			log.error("Error updating quality score: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update quality score", ex);
		}
	}

	void calculateNullPercentage(DatasetColumn column, Map<String, Object> stats, Dataset dataset) {
		if (column == null || stats == null || dataset == null) {
			stats.put("nullPercentage", 0.0);
			return;
		}
		int totalRows = dataset.getRowCount();
		int nullCount = column.getNullCount();
		if (totalRows == 0) {
			stats.put("nullPercentage", 0.0);
			return;
		}
		double percentage = (double) nullCount / totalRows * 100;
		stats.put("nullPercentage", Math.round(percentage * 100.0) / 100.0);
	}
}