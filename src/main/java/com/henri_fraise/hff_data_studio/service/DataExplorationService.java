package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.entity.ExplorationReport;
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
public class DataExplorationService {

	private final ExplorationReportRepository reportRepository;
	private final ExplorationReportMapper reportMapper;
	private final DatasetService datasetService;
	private final DatasetColumnService columnService;

	@Transactional
	public ExplorationReport generateExplorationReport(UUID datasetId) {
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
		return saved;
	}

	@Transactional
	public ExplorationReportResponse getExplorationReport(UUID datasetId) {
		ExplorationReport report = reportRepository.findByDatasetId(datasetId)
				.orElse(null);
		if (report == null) {
			report = generateExplorationReport(datasetId);
		}
		return reportMapper.toResponse(report);
	}

	@Transactional
	public ExplorationReportResponse getExplorationReportWithStats(UUID datasetId) {
		ExplorationReportResponse response = getExplorationReport(datasetId);
		Map<String, Object> columnStats = getColumnStatistics(datasetId);
		response.setColumnStatistics(columnStats);
		return response;
	}

	public Map<String, Object> getColumnStatistics(UUID datasetId) {
		List<DatasetColumn> columns = columnService.getColumnsByDataset(datasetId);
		Map<String, Object> stats = new HashMap<>();

		for (DatasetColumn column : columns) {
			Map<String, Object> colStats = new HashMap<>();
			colStats.put("originalName", column.getOriginalName());
			colStats.put("normalizedName", column.getNormalizedName());
			colStats.put("detectedType", column.getDetectedType());
			colStats.put("targetType", column.getTargetType());
			colStats.put("nullCount", column.getNullCount());
			colStats.put("uniqueCount", column.getUniqueCount());

			Dataset dataset = column.getDataset();
			if (dataset != null && dataset.getRowCount() > 0) {
				calculateNullPercentage(column, colStats, dataset);
			}

			stats.put(column.getId().toString(), colStats);
		}
		return stats;
	}

	static void calculateNullPercentage(DatasetColumn column, Map<String, Object> colStats, Dataset dataset) {
		double nullPercentage = (double) column.getNullCount() / dataset.getRowCount() * 100;
		double uniquePercentage = (double) column.getUniqueCount() / dataset.getRowCount() * 100;
		colStats.put("nullPercentage", BigDecimal.valueOf(nullPercentage).setScale(2, RoundingMode.HALF_UP));
		colStats.put("uniquePercentage", BigDecimal.valueOf(uniquePercentage).setScale(2, RoundingMode.HALF_UP));
	}

	@Transactional
	public ExplorationReportResponse getExplorationSummary(UUID datasetId) {
		ExplorationReport report = reportRepository.findByDatasetId(datasetId)
				.orElse(null);
		if (report == null) {
			report = generateExplorationReport(datasetId);
		}
		return reportMapper.toResponse(report);
	}

	public byte[] exportExplorationReport(UUID datasetId, String format) {
		ExplorationReport report = reportRepository.findByDatasetId(datasetId)
				.orElseThrow(() -> new ResourceNotFoundException("Exploration report not found for dataset: " + datasetId));

		if ("PDF".equalsIgnoreCase(format)) {
			return generatePdfReport(report);
		} else {
			return generateHtmlReport(report);
		}
	}

	private int calculateDuplicates(Dataset dataset) {
		return 0;
	}

	private int calculateMissingValues(List<DatasetColumn> columns) {
		return columns.stream().mapToInt(DatasetColumn::getNullCount).sum();
	}

	private BigDecimal calculateQualityScore(int totalRows, int duplicateCount, int missingValuesCount,
	                                         List<DatasetColumn> columns) {
		int totalCells = totalRows * columns.size();
		if (totalCells == 0) return BigDecimal.ZERO;

		double missingRatio = (double) missingValuesCount / totalCells;
		double duplicateRatio = totalRows > 0 ? (double) duplicateCount / totalRows : 0;

		double score = (1 - missingRatio - (duplicateRatio * 0.5)) * 100;
		score = Math.clamp(score, 0, 100);

		return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
	}

	private byte[] generatePdfReport(ExplorationReport report) {
		return new byte[0];
	}

	private byte[] generateHtmlReport(ExplorationReport report) {
		return new byte[0];
	}

	public long countReports() {
		return reportRepository.count();
	}

	public double getAverageQualityScore() {
		BigDecimal avg = reportRepository.averageQualityScore();
		return 0.0;
	}

	@Transactional
	public void deleteReport(UUID datasetId) {
		ExplorationReport report = reportRepository.findByDatasetId(datasetId)
				.orElse(null);
		if (report != null) {
			reportRepository.delete(report);
			log.info("Exploration report deleted for dataset: {}", datasetId);
		}
	}
}