package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.exception.FileProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileStorageService {

  @Value("${file.upload-dir:./uploads}")
  private String uploadDir;

  public String saveFile(MultipartFile file, UUID projectId, UUID userId) throws IOException {
    Path projectDir = Paths.get(uploadDir, projectId.toString(), userId.toString());
    if (!Files.exists(projectDir)) {
      Files.createDirectories(projectDir);
    }

    String originalFilename = file.getOriginalFilename();
    String extension =
        originalFilename != null
            ? originalFilename.substring(originalFilename.lastIndexOf('.'))
            : "";
    String newFilename = UUID.randomUUID().toString() + extension;

    Path filePath = projectDir.resolve(newFilename);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    return filePath.toString();
  }

  public void deleteFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    if (Files.exists(path)) {
      Files.delete(path);
      log.info("File deleted: {}", filePath);
    }
  }

  public List<Dataset> extractDatasets(SourceFile sourceFile) {
    List<Dataset> datasets = new ArrayList<>();

    try {
      Dataset dataset =
          Dataset.builder()
              .sourceFile(sourceFile)
              .datasetName("Dataset from " + sourceFile.getFileName())
              .rowCount(0)
              .columnCount(0)
              .isCleaned(false)
              .build();
      datasets.add(dataset);

      log.info("Extracted {} datasets from file: {}", datasets.size(), sourceFile.getFileName());
    } catch (Exception ex) {
      log.error("Error extracting datasets from file: {}", ex.getMessage(), ex);
      throw new FileProcessingException(sourceFile.getFileName(), "Failed to extract datasets");
    }

    return datasets;
  }

  public byte[] readFile(String filePath) throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new FileProcessingException("File not found: " + filePath);
    }
    return Files.readAllBytes(path);
  }

  public String getFileExtension(String fileName) {
    if (fileName == null) {
      return "";
    }
    int lastDot = fileName.lastIndexOf('.');
    return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
  }
}
