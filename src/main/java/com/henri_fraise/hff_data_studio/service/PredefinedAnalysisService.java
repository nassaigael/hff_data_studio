package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.PredefinedAnalysisRequest;
import com.henri_fraise.hff_data_studio.dto.response.PredefinedAnalysisResponse;
import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.PredefinedAnalysisMapper;
import com.henri_fraise.hff_data_studio.repository.PredefinedAnalysisRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredefinedAnalysisService {

  private final PredefinedAnalysisRepository analysisRepository;
  private final PredefinedAnalysisMapper analysisMapper;

  public List<PredefinedAnalysisResponse> getAllAnalyses() {
    List<PredefinedAnalysis> analyses = analysisRepository.findAll();
    return analyses.stream().map(analysisMapper::toResponse).toList();
  }

  public List<PredefinedAnalysisResponse> getAnalysesByCategory(AnalysisCategory category) {
    List<PredefinedAnalysis> analyses = analysisRepository.findByCategory(category);
    return analyses.stream().map(analysisMapper::toResponse).toList();
  }

  public PredefinedAnalysis getAnalysisEntityById(UUID analysisId) {
    return analysisRepository
        .findById(analysisId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Predefined analysis not found: " + analysisId));
  }

  public PredefinedAnalysisResponse getAnalysisById(UUID analysisId) {
    PredefinedAnalysis analysis = getAnalysisEntityById(analysisId);
    return analysisMapper.toResponse(analysis);
  }

  @Transactional
  public PredefinedAnalysis createAnalysis(PredefinedAnalysisRequest request) {
    PredefinedAnalysis analysis =
        PredefinedAnalysis.builder()
            .analysisName(request.getAnalysisName())
            .description(request.getDescription())
            .category(request.getCategory())
            .referenceScript(request.getReferenceScript())
            .requiredParametersJson(request.getRequiredParametersJson())
            .build();
    PredefinedAnalysis saved = analysisRepository.save(analysis);
    log.info("Predefined analysis created: {}", saved.getAnalysisName());
    return saved;
  }

  @Transactional
  public PredefinedAnalysis updateAnalysis(UUID analysisId, PredefinedAnalysisRequest request) {
    PredefinedAnalysis analysis = getAnalysisEntityById(analysisId);
    analysis.setAnalysisName(request.getAnalysisName());
    analysis.setDescription(request.getDescription());
    analysis.setCategory(request.getCategory());
    analysis.setReferenceScript(request.getReferenceScript());
    analysis.setRequiredParametersJson(request.getRequiredParametersJson());
    PredefinedAnalysis updated = analysisRepository.save(analysis);
    log.info("Predefined analysis updated: {}", analysisId);
    return updated;
  }

  @Transactional
  public void deleteAnalysis(UUID analysisId) {
    PredefinedAnalysis analysis = getAnalysisEntityById(analysisId);
    analysisRepository.delete(analysis);
    log.info("Predefined analysis deleted: {}", analysisId);
  }

  public long countAnalyses() {
    return analysisRepository.count();
  }

  public long countAnalysesByCategory(AnalysisCategory category) {
    return analysisRepository.countByCategory(category);
  }

  public List<PredefinedAnalysis> getAnalysesByCategoryEntity(AnalysisCategory category) {
    return analysisRepository.findByCategory(category);
  }

  public List<AnalysisCategory> getAllCategories() {
    return analysisRepository.findDistinctCategories();
  }

  public PredefinedAnalysis getDefaultAnalysisForCategory(AnalysisCategory category) {
    List<PredefinedAnalysis> analyses = analysisRepository.findByCategory(category);
    return analyses.isEmpty() ? null : analyses.get(0);
  }
}
