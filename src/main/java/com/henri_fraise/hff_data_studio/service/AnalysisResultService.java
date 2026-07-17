package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.AnalysisResultResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.entity.Chart;
import com.henri_fraise.hff_data_studio.enums.FileFormat;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.AnalysisResultMapper;
import com.henri_fraise.hff_data_studio.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultService {

	private final AnalysisResultRepository resultRepository;
	private final AnalysisResultMapper resultMapper;
	private final ChartService chartService;

	@Transactional
	public AnalysisResult createResult(AnalysisExecution execution, ResultType resultType,
	                                   String title, String filePath, FileFormat fileFormat,
	                                   Integer displayOrder) {
		AnalysisResult result = AnalysisResult.builder()
				.execution(execution)
				.resultType(resultType)
				.title(title)
				.filePath(filePath)
				.fileFormat(fileFormat)
				.displayOrder(displayOrder != null ? displayOrder : 0)
				.build();
		AnalysisResult saved = resultRepository.save(result);
		log.info("Analysis result created: {}", saved.getId());
		return saved;
	}

	@Transactional
	public AnalysisResult createResultWithChart(AnalysisExecution execution, ResultType resultType,
	                                            String title, String filePath, String fileFormat,
	                                            String chartType, String configJson) {
		AnalysisResult result = createResult(execution, resultType, title, filePath, FileFormat.valueOf(fileFormat), 0);
		Chart chart = chartService.createChart(result, chartType, configJson);
		result.setChart(chart);
		return resultRepository.save(result);
	}

	public AnalysisResult getResultEntityById(UUID resultId) {
		return resultRepository.findById(resultId)
				.orElseThrow(() -> new ResourceNotFoundException("Analysis result not found: " + resultId));
	}

	public AnalysisResultResponse getResultById(UUID resultId) {
		AnalysisResult result = getResultEntityById(resultId);
		return resultMapper.toResponse(result);
	}

	public List<AnalysisResultResponse> getResultsByExecution(UUID executionId) {
		List<AnalysisResult> results = resultRepository.findByExecutionIdOrderByDisplayOrderAsc(executionId);
		return results.stream().map(resultMapper::toResponse).toList();
	}

	public List<AnalysisResult> getResultsByExecutionEntity(AnalysisExecution execution) {
		return resultRepository.findByExecutionOrderByDisplayOrderAsc(execution);
	}

	public List<AnalysisResult> getResultsByType(ResultType resultType) {
		return resultRepository.findByResultType(resultType);
	}

	public ResponseEntity<byte[]> downloadResult(UUID resultId) {
		AnalysisResult result = getResultEntityById(resultId);
		Path filePath = Paths.get(result.getFilePath());

		try {
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				throw new RuntimeException("File not found: " + result.getFilePath());
			}

			String contentType = switch (result.getFileFormat()) {
				case CSV -> "text/csv";
				case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
				case PNG -> "image/png";
				case SVG -> "image/svg+xml";
				default -> "application/octet-stream";
			};

			byte[] content = resource.getContentAsByteArray();

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + result.getTitle() + "." + result.getFileFormat() + "\"")
					.body(content);
		} catch (Exception e) {
			log.error("Error downloading result: {}", e.getMessage());
			throw new RuntimeException("Error downloading file", e);
		}
	}

	@Transactional
	public void deleteResult(UUID resultId) {
		AnalysisResult result = getResultEntityById(resultId);
		resultRepository.delete(result);
		log.info("Result deleted: {}", resultId);
	}

	@Transactional
	public void deleteResultsByExecution(UUID executionId) {
		List<AnalysisResult> results = resultRepository.findByExecutionIdOrderByDisplayOrderAsc(executionId);
		resultRepository.deleteAll(results);
		log.info("Deleted {} results for execution: {}", results.size(), executionId);
	}

	public long countResultsByExecution(UUID executionId) {
		return resultRepository.countByExecutionId(executionId);
	}
}