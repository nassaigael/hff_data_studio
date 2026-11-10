package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.StatsResponse;
import com.henri_fraise.hff_data_studio.repository.DatasetRepository;
import com.henri_fraise.hff_data_studio.repository.projection.DatasetStatsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsService {

    private static final String ALL_PROJECTS = "all";

    private final DatasetRepository datasetRepository;

    public StatsResponse getStats(String projectId) {
        String effectiveProjectId = normalize(projectId);
        DatasetStatsProjection projection = datasetRepository.findGlobalStats(effectiveProjectId);
        return new StatsResponse(
                projection.getTotalDatasets(),
                projection.getTotalRows(),
                projection.getTotalColumns(),
                projection.getCleanedDatasets(),
                projection.getUncleanedDatasets(),
                projection.getAvgQualityScore(),
                projection.getNewDatasetsLastMonth(),
                projection.getTotalAnalysesRun(),
                projection.getTotalExports());
    }

    private String normalize(String projectId) {
        if (projectId == null || projectId.isBlank() || ALL_PROJECTS.equalsIgnoreCase(projectId)) {
            return null;
        }
        return projectId;
    }
}