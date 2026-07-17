package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PythonAnalysisService {

	private final RestTemplate restTemplate;
	@Getter
	private final ObjectMapper objectMapper;
	private final AnalysisExecutionService executionService;
	private final AnalysisResultService resultService;
	private final DatasetService datasetService;
	@Getter
	private final PredefinedAnalysisService predefinedAnalysisService;

	@Value("${python.service.url:http://localhost:5000}")
	private String pythonServiceUrl;

	public void executeAnalysis(UUID executionId) {
		try {
			AnalysisExecution execution = executionService.getExecutionEntityById(executionId);
			Dataset dataset = execution.getDataset();
			PredefinedAnalysis analysis = execution.getPredefinedAnalysis();

			String scriptPath = analysis != null ? analysis.getReferenceScript() : "custom_analysis.py";
			String dataPath = datasetService.getDatasetFilePath(dataset.getId());

			Map<String, Object> request = Map.of(
					"executionId", executionId.toString(),
					"datasetId", dataset.getId().toString(),
					"dataPath", dataPath,
					"scriptPath", scriptPath,
					"parameters", execution.getUsedParametersJson() != null
							? execution.getUsedParametersJson()
							: Map.of()
			);

			long startTime = System.currentTimeMillis();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

			String url = UriComponentsBuilder.fromUri(URI.create(pythonServiceUrl))
					.path("/api/execute")
					.build()
					.toUriString();

			ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

			long duration = System.currentTimeMillis() - startTime;

			if (response.getStatusCode() == HttpStatus.OK) {
				Map<String, Object> responseBody = response.getBody();
				if (responseBody != null && "SUCCESS".equals(responseBody.get("status"))) {
					handleSuccessResponse(execution, responseBody, duration);
				} else {
					handleErrorResponse(execution, responseBody != null
							? (String) responseBody.get("error")
							: "Unknown error", duration);
				}
			} else {
				handleErrorResponse(execution, "Python service returned status: " + response.getStatusCode(), duration);
			}

		} catch (Exception e) {
			log.error("Error executing analysis: {}", e.getMessage(), e);
			executionService.updateExecutionStatus(executionId, AnalysisExecutionStatus.ERROR, e.getMessage());
		}
	}

	private void handleSuccessResponse(AnalysisExecution execution, Map<String, Object> response, long duration) {
		try {
			List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
			List<UUID> resultIds = new ArrayList<>();

			if (results != null) {
				for (Map<String, Object> resultData : results) {
					String resultType = (String) resultData.get("type");
					String title = (String) resultData.get("title");
					String filePath = (String) resultData.get("filePath");
					String fileFormat = (String) resultData.get("format");

					ResultType type = ResultType.valueOf(resultType.toUpperCase());

					UUID resultId = resultService.createResult(
							execution,
							type,
							title,
							filePath,
							FileType.valueOf(fileFormat),
							(Integer) resultData.getOrDefault("displayOrder", 0)
					).getId();
					resultIds.add(resultId);

					if (resultData.containsKey("chartType")) {
						String chartType = (String) resultData.get("chartType");
						String configJson = (String) resultData.get("configJson");
						resultService.createResultWithChart(
								execution,
								type,
								title,
								filePath,
								fileFormat,
								chartType,
								configJson
						);
					}
				}
			}

			executionService.updateExecutionWithResults(
					execution.getId(),
					AnalysisExecutionStatus.COMPLETED,
					(int) duration,
					execution.getUsedParametersJson()
			);

			log.info("Analysis completed successfully: {} with {} results", execution.getId(), resultIds.size());

		} catch (Exception e) {
			log.error("Error handling successful response: {}", e.getMessage(), e);
			executionService.updateExecutionStatus(execution.getId(), AnalysisExecutionStatus.ERROR, e.getMessage());
		}
	}

	private void handleErrorResponse(AnalysisExecution execution, String errorMessage, long duration) {
		executionService.updateExecutionStatus(execution.getId(), AnalysisExecutionStatus.ERROR, errorMessage);
		log.error("Analysis execution failed: {} - {}", execution.getId(), errorMessage);
	}

	public String getPythonServiceStatus() {
		try {
			String url = UriComponentsBuilder.fromUri(URI.create(pythonServiceUrl))
					.path("/api/health")
					.build()
					.toUriString();
			ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
			return response.getBody() != null ? (String) response.getBody().get("status") : "UNKNOWN";
		} catch (Exception e) {
			log.warn("Python service health check failed: {}", e.getMessage());
			return "UNAVAILABLE";
		}
	}

	public Map<String, Object> getAnalysisPreview(UUID datasetId, String analysisType, Map<String, Object> params) {
		try {
			String dataPath = datasetService.getDatasetFilePath(datasetId);

			Map<String, Object> request = Map.of(
					"dataPath", dataPath,
					"analysisType", analysisType,
					"parameters", params,
					"preview", true
			);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

			String url = UriComponentsBuilder.fromUri(URI.create(pythonServiceUrl))
					.path("/api/preview")
					.build()
					.toUriString();

			ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
			return response.getBody();

		} catch (Exception e) {
			log.error("Error getting analysis preview: {}", e.getMessage());
			throw new RuntimeException("Error getting analysis preview", e);
		}
	}

	public void cancelExecution(UUID executionId) {
		try {
			String url = UriComponentsBuilder.fromUri(URI.create(pythonServiceUrl))
					.path("/api/cancel/{executionId}")
					.buildAndExpand(executionId.toString())
					.toUriString();

			restTemplate.postForEntity(url, null, Void.class);
			executionService.updateExecutionStatus(executionId, AnalysisExecutionStatus.CANCELLED, 1);
			log.info("Execution cancelled: {}", executionId);

		} catch (Exception e) {
			log.error("Error cancelling execution: {}", e.getMessage());
			throw new RuntimeException("Error cancelling execution", e);
		}
	}

}