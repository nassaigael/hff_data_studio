package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.ExplorationReportResponse;
import com.henri_fraise.hff_data_studio.service.ExplorationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/datasets/{datasetId}/exploration")
@RequiredArgsConstructor
public class ExplorationController {

  private final ExplorationService explorationService;

  @GetMapping("/report")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<ExplorationReportResponse> getExplorationReport(
      @PathVariable UUID datasetId) {
    return ResponseEntity.ok(explorationService.getExplorationReport(datasetId));
  }

  @PostMapping("/report")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<ExplorationReportResponse> generateExplorationReport(
      @PathVariable UUID datasetId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(explorationService.generateExplorationReport(datasetId));
  }

  @GetMapping("/report/export")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public byte[] exportExplorationReport(
      @PathVariable UUID datasetId, @RequestParam(defaultValue = "PDF") String format) {
    return explorationService.exportExplorationReport(datasetId, format);
  }

  @GetMapping("/summary")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<ExplorationReportResponse> getExplorationSummary(
      @PathVariable UUID datasetId) {
    return ResponseEntity.ok(explorationService.getExplorationSummary(datasetId));
  }
}
