package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.DatasetResponse;
import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.DatasetMapper;
import com.henri_fraise.hff_data_studio.mapper.SourceFileMapper;
import com.henri_fraise.hff_data_studio.repository.SourceFileRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

  private final SourceFileRepository fileRepository;
  private final SourceFileMapper fileMapper;
  private final DatasetMapper datasetMapper;
  private final ProjectService projectService;
  private final UserService userService;
  private final DatasetService datasetService;
  private final SecurityUtils securityUtils;

  @Value("${file.upload-dir:./uploads}")
  private String uploadDir;

  @Value("${file.max-size:200}")
  private Long maxFileSizeMb;

  @Transactional
  public SourceFileResponse uploadFile(MultipartFile file, UUID projectId, String fileType) {
    try {
      Project project = projectService.getProjectEntityById(projectId);
      User user = userService.getUserEntityById(getCurrentUserId());

      validateFile(file);

      String originalFilename = file.getOriginalFilename();
      String extension = getFileExtension(originalFilename);
      String storageFileName = generateStorageFileName(originalFilename);
      Path storagePath = Paths.get(uploadDir, projectId.toString(), storageFileName);

      Files.createDirectories(storagePath.getParent());
      Files.copy(file.getInputStream(), storagePath, StandardCopyOption.REPLACE_EXISTING);

      FileType detectedType = detectFileType(fileType, extension);

      SourceFile sourceFile =
          SourceFile.builder()
              .fileName(originalFilename)
              .fileType(detectedType)
              .storagePath(storagePath.toString())
              .sizeBytes(file.getSize())
              .uploadedAt(LocalDateTime.now())
              .processingStatus(FileProcessingStatus.RECEIVED)
              .project(project)
              .user(user)
              .build();

      SourceFile saved = fileRepository.save(sourceFile);
      log.info(
          "File uploaded: {} ({} bytes) for project: {}",
          saved.getFileName(),
          saved.getSizeBytes(),
          projectId);

      processFileContent(saved);

      return fileMapper.toResponse(saved);

    } catch (IOException e) {
      log.error("Error uploading file: {}", e.getMessage());
      throw new RuntimeException("Failed to upload file: " + e.getMessage());
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    long maxSizeBytes = maxFileSizeMb * 1024 * 1024;
    if (file.getSize() > maxSizeBytes) {
      throw new IllegalArgumentException(
          "File size exceeds maximum allowed: " + maxFileSizeMb + "MB");
    }

    String filename = file.getOriginalFilename();
    if (filename == null || filename.isEmpty()) {
      throw new IllegalArgumentException("File name is invalid");
    }

    String extension = getFileExtension(filename).toLowerCase();
    if (!isValidExtension(extension)) {
      throw new IllegalArgumentException("File type not supported: " + extension);
    }
  }

  private boolean isValidExtension(String extension) {
    return List.of("csv", "xlsx", "xls", "sql", "txt").contains(extension);
  }

  private FileType detectFileType(String fileType, String extension) {
    if (fileType != null) {
      try {
        return FileType.valueOf(fileType.toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Invalid file type provided: {}, using detection", fileType);
      }
    }

    return switch (extension.toLowerCase()) {
      case "xlsx", "xls" -> FileType.EXCEL;
      case "sql" -> FileType.SQL;
      default -> FileType.CSV;
    };
  }

  private String getFileExtension(String filename) {
    if (filename == null) {
      return "";
    }
    int lastDot = filename.lastIndexOf('.');
    return lastDot > 0 ? filename.substring(lastDot + 1) : "";
  }

  private String generateStorageFileName(String originalFilename) {
    String extension = getFileExtension(originalFilename);
    String uuid = UUID.randomUUID().toString();
    return uuid + (extension.isEmpty() ? "" : "." + extension);
  }

  private void processFileContent(SourceFile file) {
    try {
      file.setProcessingStatus(FileProcessingStatus.ANALYZING);
      fileRepository.save(file);

      Dataset dataset = datasetService.extractDatasetFromFile(file);

      file.setProcessingStatus(FileProcessingStatus.EXPLORED);
      fileRepository.save(file);

      log.info("File processed: {} -> dataset: {}", file.getId(), dataset.getId());

    } catch (Exception e) {
      log.error("Error processing file content: {}", e.getMessage());
      file.setProcessingStatus(FileProcessingStatus.ERROR);
      fileRepository.save(file);
      throw new RuntimeException("Failed to process file: " + e.getMessage());
    }
  }

  public SourceFile getFileEntityById(UUID fileId) {
    return fileRepository
        .findById(fileId)
        .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
  }

  public SourceFileResponse getFileById(UUID fileId) {
    SourceFile file = getFileEntityById(fileId);
    return fileMapper.toResponse(file);
  }

  public List<SourceFileResponse> getFilesByProject(UUID projectId) {
    List<SourceFile> files = fileRepository.findByProjectIdOrderByUploadedAtDesc(projectId);
    return files.stream().map(fileMapper::toResponse).toList();
  }

  public Page<SourceFileResponse> getFilesByProject(UUID projectId, Pageable pageable) {
    Page<SourceFile> files = fileRepository.findByProjectId(projectId, pageable);
    return files.map(fileMapper::toResponse);
  }

  public List<SourceFileResponse> getFilesByUser(UUID userId) {
    List<SourceFile> files = fileRepository.findByUserIdOrderByUploadedAtDesc(userId);
    return files.stream().map(fileMapper::toResponse).toList();
  }

  public List<SourceFileResponse> getFilesByStatus(FileProcessingStatus status) {
    List<SourceFile> files = fileRepository.findByProcessingStatus(status);
    return files.stream().map(fileMapper::toResponse).toList();
  }

  public List<DatasetResponse> getDatasetsByFile(UUID fileId) {
    SourceFile file = getFileEntityById(fileId);
    List<Dataset> datasets = file.getDatasets();
    return datasets.stream().map(datasetMapper::toResponse).toList();
  }

  @Transactional
  public void deleteFile(UUID fileId) {
    SourceFile file = getFileEntityById(fileId);

    try {
      Path filePath = Paths.get(file.getStoragePath());
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      log.warn(" Could not delete physical file: {}", e.getMessage());
    }

    datasetService.deleteDatasetsByFile(fileId);

    fileRepository.delete(file);
    log.info("File deleted: {}", fileId);
  }

  @Transactional
  public void deleteFilesByProject(UUID projectId) {
    List<SourceFile> files = fileRepository.findByProjectIdOrderByUploadedAtDesc(projectId);
    for (SourceFile file : files) {
      try {
        Path filePath = Paths.get(file.getStoragePath());
        Files.deleteIfExists(filePath);
      } catch (IOException e) {
        log.warn("Could not delete physical file: {}", e.getMessage());
      }
      datasetService.deleteDatasetsByFile(file.getId());
    }
    fileRepository.deleteAll(files);
    log.info("Deleted {} files for project: {}", files.size(), projectId);
  }

  public ResponseEntity<byte[]> downloadFile(UUID fileId) {
    SourceFile file = getFileEntityById(fileId);
    Path filePath = Paths.get(file.getStoragePath());

    try {
      Resource resource = new UrlResource(filePath.toUri());
      if (!resource.exists()) {
        throw new RuntimeException("File not found: " + file.getStoragePath());
      }

      String contentType = getContentType(file.getFileType());
      byte[] content = resource.getContentAsByteArray();

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=\"" + file.getFileName() + "\"")
          .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length))
          .body(content);

    } catch (Exception e) {
      log.error("Error downloading file: {}", e.getMessage());
      throw new RuntimeException("Error downloading file", e);
    }
  }

  public Resource downloadFileAsResource(UUID fileId) {
    SourceFile file = getFileEntityById(fileId);
    Path filePath = Paths.get(file.getStoragePath());

    try {
      Resource resource = new UrlResource(filePath.toUri());
      if (!resource.exists()) {
        throw new RuntimeException("File not found: " + file.getStoragePath());
      }
      return resource;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error accessing file", e);
    }
  }

  private String getContentType(FileType fileType) {
    return switch (fileType) {
      case CSV -> "text/csv";
      case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      case SQL -> "text/plain";
      default -> "application/octet-stream";
    };
  }

  @Transactional
  public SourceFile updateFileStatus(UUID fileId, FileProcessingStatus status) {
    SourceFile file = getFileEntityById(fileId);
    file.setProcessingStatus(status);
    SourceFile updated = fileRepository.save(file);
    log.info("File status updated: {} -> {}", fileId, status);
    return updated;
  }

  public long countFiles() {
    return fileRepository.count();
  }

  public long countFilesByProject(UUID projectId) {
    return fileRepository.countByProjectId(projectId);
  }

  public long countFilesByUser(UUID userId) {
    return fileRepository.countByUserId(userId);
  }

  public long countFilesByStatus(FileProcessingStatus status) {
    return fileRepository.countByProcessingStatus(status);
  }

  public long getTotalFileSize() {
    Long total = fileRepository.sumFileSizes();
    return total != null ? total : 0L;
  }

  public long getAverageFileSize() {
    Double avg = fileRepository.averageFileSize();
    return avg != null ? avg.longValue() : 0L;
  }

  public long getTotalFileSizeByProject(UUID projectId) {
    Long total = fileRepository.sumFileSizesByProjectId(projectId);
    return total != null ? total : 0L;
  }

  public List<SourceFile> getFilesOlderThan(LocalDateTime date) {
    return fileRepository.findByUploadedAtBefore(date);
  }

  public List<SourceFile> getFilesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return fileRepository.findByUploadedAtBetween(startDate, endDate);
  }

  @Transactional
  public void cleanupOldFiles(LocalDateTime thresholdDate) {
    List<SourceFile> oldFiles = getFilesOlderThan(thresholdDate);
    for (SourceFile file : oldFiles) {
      deleteFile(file.getId());
    }
    log.info("Cleaned up {} old files", oldFiles.size());
  }

  public boolean fileExists(UUID fileId) {
    return fileRepository.existsById(fileId);
  }

  public boolean fileExistsByName(String fileName, UUID projectId) {
    return fileRepository.existsByFileNameAndProjectId(fileName, projectId);
  }

  public List<SourceFile> getFilesByType(FileType fileType) {
    return fileRepository.findByFileType(fileType);
  }

  public long countFilesByType(FileType fileType) {
    return fileRepository.countByFileType(fileType);
  }

  public Page<SourceFileResponse> getAllFiles(Pageable pageable) {
    Page<SourceFile> files = fileRepository.findAll(pageable);
    return files.map(fileMapper::toResponse);
  }

  public List<SourceFile> getFilesByProjectEntity(Project project) {
    return fileRepository.findByProjectIdOrderByUploadedAtDesc(project.getId());
  }

  @Transactional
  public SourceFile updateFileMetadata(UUID fileId, String fileName, String fileType) {
    SourceFile file = getFileEntityById(fileId);

    if (fileName != null && !fileName.isEmpty()) {
      file.setFileName(fileName);
    }

    if (fileType != null) {
      try {
        file.setFileType(FileType.valueOf(fileType.toUpperCase()));
      } catch (IllegalArgumentException e) {
        log.warn("Invalid file type: {}", fileType);
      }
    }

    SourceFile updated = fileRepository.save(file);
    log.info("File metadata updated: {}", fileId);
    return updated;
  }

  public String getFileContentPreview(UUID fileId, int maxLines) {
    SourceFile file = getFileEntityById(fileId);
    Path filePath = Paths.get(file.getStoragePath());

    try {
      List<String> lines = Files.readAllLines(filePath);
      int lineCount = Math.min(maxLines, lines.size());
      return String.join("\n", lines.subList(0, lineCount));
    } catch (IOException e) {
      log.error("Error reading file preview: {}", e.getMessage());
      throw new RuntimeException("Error reading file preview", e);
    }
  }

  public Map<String, Object> getFileStatistics(UUID fileId) {
    SourceFile file = getFileEntityById(fileId);
    Map<String, Object> stats = new HashMap<>();
    stats.put("fileId", file.getId());
    stats.put("fileName", file.getFileName());
    stats.put("fileType", file.getFileType());
    stats.put("sizeBytes", file.getSizeBytes());
    stats.put("sizeFormatted", formatFileSize(file.getSizeBytes()));
    stats.put("uploadedAt", file.getUploadedAt());
    stats.put("processingStatus", file.getProcessingStatus());
    stats.put("datasetCount", file.getDatasets() != null ? file.getDatasets().size() : 0);
    stats.put("projectId", file.getProject() != null ? file.getProject().getId() : null);
    stats.put("projectName", file.getProject() != null ? file.getProject().getProjectName() : null);
    stats.put("userId", file.getUser() != null ? file.getUser().getId() : null);
    stats.put(
        "userFullName",
        file.getUser() != null
            ? file.getUser().getFirstName() + " " + file.getUser().getLastName()
            : null);
    return stats;
  }

  private String formatFileSize(long bytes) {
    return getString(bytes);
  }

  @NonNull
  static String getString(long bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
    if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
    return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
  }

  private UUID getCurrentUserId() {
    return securityUtils.getCurrentUserId();
  }
}
