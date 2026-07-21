package com.henri_fraise.hff_data_studio.controller;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.mapper.PageMapper;
import com.henri_fraise.hff_data_studio.mapper.SourceFileMapper;
import com.henri_fraise.hff_data_studio.service.FileService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

  private final FileService fileService;
  private final PageMapper pageMapper;
  private final SourceFileMapper fileMapper;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority('FILE_UPLOAD')")
  public ResponseEntity<SourceFileResponse> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("projectId") UUID projectId,
      @RequestParam(value = "fileType", required = false) String fileType) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(fileService.uploadFile(file, projectId, fileType));
  }

  @GetMapping("/{fileId}")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<SourceFileResponse> getFileById(@PathVariable UUID fileId) {
    return ResponseEntity.ok(fileService.getFileById(fileId));
  }

  @DeleteMapping("/{fileId}")
  @PreAuthorize("hasAuthority('FILE_DELETE')")
  public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
    fileService.deleteFile(fileId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/project/{projectId}")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<List<SourceFileResponse>> getFilesByProject(@PathVariable UUID projectId) {
    return ResponseEntity.ok(fileService.getFilesByProject(projectId));
  }

  @GetMapping("/project/{projectId}/page")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<PageResponse<SourceFileResponse>> getFilesByProjectPaged(
      @PathVariable UUID projectId, @PageableDefault(size = 20) Pageable pageable) {
    Page<SourceFileResponse> files = fileService.getFilesByProject(projectId, pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(files, f -> f));
  }

  @GetMapping("/{fileId}/datasets")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<List<DatasetResponse>> getDatasetsByFile(@PathVariable UUID fileId) {
    return ResponseEntity.ok(fileService.getDatasetsByFile(fileId));
  }

  @GetMapping("/{fileId}/download")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) {
    return fileService.downloadFile(fileId);
  }

  @GetMapping("/{fileId}/preview")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<String> getFilePreview(
      @PathVariable UUID fileId, @RequestParam(defaultValue = "10") int maxLines) {
    return ResponseEntity.ok(fileService.getFileContentPreview(fileId, maxLines));
  }

  @GetMapping("/{fileId}/statistics")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<Map<String, Object>> getFileStatistics(@PathVariable UUID fileId) {
    return ResponseEntity.ok(fileService.getFileStatistics(fileId));
  }

  @GetMapping("/status/{status}")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<List<SourceFileResponse>> getFilesByStatus(
      @PathVariable FileProcessingStatus status) {
    return ResponseEntity.ok(fileService.getFilesByStatus(status));
  }

  @PatchMapping("/{fileId}/status")
  @PreAuthorize("hasAuthority('FILE_UPLOAD')")
  public ResponseEntity<SourceFileResponse> updateFileStatus(
      @PathVariable UUID fileId, @RequestParam FileProcessingStatus status) {
    return ResponseEntity.ok(fileMapper.toResponse(fileService.updateFileStatus(fileId, status)));
  }

  @GetMapping("/all")
  @PreAuthorize("hasAuthority('FILE_VIEW')")
  public ResponseEntity<PageResponse<SourceFileResponse>> getAllFiles(
      @PageableDefault(size = 20) Pageable pageable) {
    Page<SourceFileResponse> files = fileService.getAllFiles(pageable);
    return ResponseEntity.ok(pageMapper.toPageResponse(files, f -> f));
  }
}
