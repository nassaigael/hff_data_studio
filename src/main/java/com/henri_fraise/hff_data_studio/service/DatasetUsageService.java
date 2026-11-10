package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetAnalysisHistoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetExportHistoryResponse;
import com.henri_fraise.hff_data_studio.dto.response.DatasetUsageSummaryResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.EntityTag;
import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.exception.DatabaseException;
import com.henri_fraise.hff_data_studio.repository.AnalysisExecutionRepository;
import com.henri_fraise.hff_data_studio.repository.CommentRepository;
import com.henri_fraise.hff_data_studio.repository.EntityTagRepository;
import com.henri_fraise.hff_data_studio.repository.ExportRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DatasetUsageService {

	private static final int RECENT_LIMIT = 20;
	private static final String ENTITY_TYPE_DATASET = "DATASET";

	private final AnalysisExecutionRepository executionRepository;
	private final ExportRepository exportRepository;
	private final CommentRepository commentRepository;
	private final EntityTagRepository entityTagRepository;

	public DatasetUsageSummaryResponse getSummary(UUID datasetId) {
		try {
			long analysesCount = executionRepository.countByDatasetId(datasetId);
			long exportsCount = exportRepository.countByDatasetId(datasetId);
			long uniqueUsersCount = executionRepository.countDistinctUsersByDatasetId(datasetId);
			long commentsCount =
					commentRepository.countByEntityTypeAndEntityIdAndIsDeletedFalse(
							ENTITY_TYPE_DATASET, datasetId);
			LocalDateTime lastActivityAt = executionRepository.findLastExecutionAt(datasetId);
			List<String> tags = resolveTagNames(datasetId);

			return new DatasetUsageSummaryResponse(
					analysesCount, exportsCount, uniqueUsersCount, commentsCount, lastActivityAt, tags);
		} catch (Exception ex) {
			log.error("Error computing usage summary: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to compute usage summary", ex);
		}
	}

	public List<DatasetAnalysisHistoryResponse> getAnalysesHistory(UUID datasetId) {
		try {
			List<AnalysisExecution> executions =
					executionRepository.findRecentByDatasetId(
							datasetId, PageRequest.of(0, RECENT_LIMIT));

			List<DatasetAnalysisHistoryResponse> result = new ArrayList<>(executions.size());
			for (AnalysisExecution ae : executions) {
				String analysisName =
						ae.getPredefinedAnalysis() != null ? ae.getPredefinedAnalysis().getAnalysisName() : null;
				String analysisCategory =
						ae.getPredefinedAnalysis() != null && ae.getPredefinedAnalysis().getCategory() != null
								? String.valueOf(ae.getPredefinedAnalysis().getCategory())
								: null;
				String status = ae.getStatus() != null ? String.valueOf(ae.getStatus()) : null;
				String userName = resolveUserName(ae);
				String email = ae.getUser() != null ? ae.getUser().getEmail() : null;

				result.add(
						new DatasetAnalysisHistoryResponse(
								ae.getId(),
								analysisName,
								analysisCategory,
								status,
								ae.getExecutedAt(),
								ae.getDurationMs(),
								userName,
								email));
			}
			return result;
		} catch (Exception ex) {
			log.error("Error fetching analyses history: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to fetch analyses history", ex);
		}
	}

	public List<DatasetExportHistoryResponse> getExportsHistory(UUID datasetId) {
		try {
			List<Export> exports =
					exportRepository.findRecentByDatasetId(datasetId, PageRequest.of(0, RECENT_LIMIT));

			List<DatasetExportHistoryResponse> result = new ArrayList<>(exports.size());
			for (Export export : exports) {
				String userName =
						export.getUser() != null
								? formatUserName(
								export.getUser().getFirstName(),
								export.getUser().getLastName(),
								export.getUser().getEmail())
								: "Inconnu";
				String email = export.getUser() != null ? export.getUser().getEmail() : null;
				String format =
						export.getExportFormat() != null ? String.valueOf(export.getExportFormat()) : "UNKNOWN";
				String downloadUrl = "/api/v1/exports/" + export.getId() + "/download";

				result.add(
						new DatasetExportHistoryResponse(
								export.getId(),
								format,
								export.getExportedAt(),
								export.getFileSize(),
								userName,
								email,
								downloadUrl));
			}
			return result;
		} catch (Exception ex) {
			log.error("Error fetching exports history: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to fetch exports history", ex);
		}
	}

	private List<String> resolveTagNames(UUID datasetId) {
		List<EntityTag> entityTags =
				entityTagRepository.findByEntityTypeAndEntityId(ENTITY_TYPE_DATASET, datasetId);
		List<String> names = new ArrayList<>(entityTags.size());
		for (EntityTag et : entityTags) {
			if (et.getTag() != null && et.getTag().getName() != null) {
				names.add(et.getTag().getName());
			}
		}
		return names;
	}

	private String resolveUserName(AnalysisExecution execution) {
		if (execution.getUser() == null) {
			return "Inconnu";
		}
		return formatUserName(
				execution.getUser().getFirstName(),
				execution.getUser().getLastName(),
				execution.getUser().getEmail());
	}

	private String formatUserName(String firstName, String lastName, String email) {
		String full =
				((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""))
						.trim();
		if (!full.isEmpty()) {
			return full;
		}
		return email != null ? email : "Inconnu";
	}
}