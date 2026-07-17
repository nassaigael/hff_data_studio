package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.SourceFileResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.Project;
import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.exception.*;
import com.henri_fraise.hff_data_studio.mapper.SourceFileMapper;
import com.henri_fraise.hff_data_studio.repository.SourceFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SourceFileService {

	private final SourceFileRepository sourceFileRepository;
	private final SourceFileMapper sourceFileMapper;
	private final ProjectService projectService;
	private final UserService userService;
	private final FileStorageService fileStorageService;
	private final DatasetService datasetService;
	private final AuditLogService auditLogService;

	public Page<SourceFileResponse> getFilesByProject(UUID projectId, Pageable pageable) {
		try {
			projectService.getProjectEntityById(projectId);
			Page<SourceFile> files = sourceFileRepository.findByProjectId(projectId, pageable);
			return files.map(sourceFileMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error retrieving files by project: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve files by project", ex);
		}
	}

	public Page<SourceFileResponse> getFilesByUser(UUID userId, Pageable pageable) {
		try {
			userService.getUserEntityById(userId);
			Page<SourceFile> files = sourceFileRepository.findByUserId(userId, pageable);
			return files.map(sourceFileMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error retrieving files by user: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve files by user", ex);
		}
	}

	public SourceFileResponse getFileById(UUID fileId) {
		SourceFile file = getFileEntityById(fileId);
		return sourceFileMapper.toResponse(file);
	}

	public SourceFile getFileEntityById(UUID fileId) {
		return sourceFileRepository.findById(fileId)
				.orElseThrow(() -> new ResourceNotFoundException("SourceFile", fileId));
	}

	@Transactional
	public SourceFileResponse uploadFile(MultipartFile file, UUID projectId, UUID userId) {
		Project project = projectService.getProjectEntityById(projectId);
		User user = userService.getUserEntityById(userId);

		if (!project.getCreator().getId().equals(userId)) {
			throw new ForbiddenException("You don't have permission to upload files to this project");
		}

		try {
			String storagePath = fileStorageService.saveFile(file, projectId, userId);
			SourceFile sourceFile = SourceFile.builder()
					.fileName(file.getOriginalFilename())
					.fileFormat(detectFileFormat(file.getOriginalFilename()))
					.storagePath(storagePath)
					.sizeBytes(file.getSize())
					.processingStatus(FileProcessingStatus.RECEIVED)
					.project(project)
					.user(user)
					.build();

			SourceFile saved = sourceFileRepository.save(sourceFile);

			log.info("File uploaded successfully: {} ({}) to project {}",
					saved.getFileName(), saved.getId(), projectId);

			auditLogService.logAction(
					"FILE_UPLOADED",
					"SourceFile",
					saved.getId(),
					"File " + saved.getFileName() + " uploaded to project " + project.getProjectName()
			);

			processFileAsync(saved);

			return sourceFileMapper.toResponse(saved);
		} catch (IOException ex) {
			log.error("Error uploading file: {}", ex.getMessage(), ex);
			throw new FileProcessingException(file.getOriginalFilename(), "Failed to save file: " + ex.getMessage());
		} catch (Exception ex) {
			log.error("Error uploading file: {}", ex.getMessage(), ex);
			throw new FileProcessingException(file.getOriginalFilename(), ex.getMessage());
		}
	}

	@Transactional
	public void updateProcessingStatus(UUID fileId, FileProcessingStatus status) {
		try {
			sourceFileRepository.updateProcessingStatus(fileId, status);
			log.info("File processing status updated: {} -> {}", fileId, status);
		} catch (Exception ex) {
			log.error("Error updating processing status: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to update processing status", ex);
		}
	}

	@Transactional
	public void deleteFile(UUID fileId, UUID userId) {
		SourceFile file = getFileEntityById(fileId);

		if (!file.getUser().getId().equals(userId) &&
				!file.getProject().getCreator().getId().equals(userId)) {
			throw new ForbiddenException("You don't have permission to delete this file");
		}

		try {
			fileStorageService.deleteFile(file.getStoragePath());

			for (Dataset dataset : file.getDatasets()) {
				datasetService.deleteDataset(dataset.getId(), userId);
			}

			sourceFileRepository.delete(file);

			log.info("File deleted successfully: {} ({})", file.getFileName(), fileId);

			auditLogService.logAction(
					"FILE_DELETED",
					"SourceFile",
					fileId,
					"File " + file.getFileName() + " deleted by " + userId
			);
		} catch (IOException ex) {
			log.error("Error deleting file: {}", ex.getMessage(), ex);
			throw new FileProcessingException(file.getFileName(), "Failed to delete file: " + ex.getMessage());
		} catch (Exception ex) {
			log.error("Error deleting file: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to delete file", ex);
		}
	}

	public Page<SourceFileResponse> searchProjectFiles(UUID projectId, String searchTerm, Pageable pageable) {
		try {
			projectService.getProjectEntityById(projectId);
			Page<SourceFile> files = sourceFileRepository.searchProjectFiles(projectId, searchTerm, pageable);
			return files.map(sourceFileMapper::toResponse);
		} catch (ResourceNotFoundException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error searching project files: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to search project files", ex);
		}
	}

	public List<SourceFileResponse> getFilesByStatus(FileProcessingStatus status) {
		try {
			List<SourceFile> files = sourceFileRepository.findByProcessingStatus(status);
			return files.stream()
					.map(sourceFileMapper::toResponse)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.error("Error retrieving files by status: {}", ex.getMessage(), ex);
			throw new DatabaseException("Failed to retrieve files by status", ex);
		}
	}

	public long countFilesByProject(UUID projectId) {
		return sourceFileRepository.countByProjectId(projectId);
	}

	public long countFilesByUser(UUID userId) {
		return sourceFileRepository.countByUserId(userId);
	}

	public long countFilesByStatus(FileProcessingStatus status) {
		return sourceFileRepository.countByProcessingStatus(status);
	}

	public Long getTotalFileSizeByProject(UUID projectId) {
		return sourceFileRepository.sumFileSizeByProjectId(projectId);
	}


	@Transactional
	protected void processFileAsync(SourceFile file) {
		updateProcessingStatus(file.getId(), FileProcessingStatus.ANALYZING);
		try {
			List<Dataset> datasets = fileStorageService.extractDatasets(file);
			for (Dataset dataset : datasets) {
				datasetService.createDataset(dataset);
			}

			updateProcessingStatus(file.getId(), FileProcessingStatus.EXPLORED);
		} catch (Exception ex) {
			log.error("Error processing file {}: {}", file.getId(), ex.getMessage(), ex);
			updateProcessingStatus(file.getId(), FileProcessingStatus.ERROR);
		}
	}

	private FileType detectFileFormat(String fileName) {
		if (fileName == null) {
			return null;
		}
		String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
		return switch (extension) {
			case "csv" -> FileType.CSV;
			case "xlsx", "xls" -> FileType.XLSX;
			case "sql" -> FileType.SQL;
			default -> throw new ValidationException("Unsupported file format: " + extension);
		};
	}

	@Transactional
	public void cleanupFailedFiles() {
		LocalDateTime threshold = LocalDateTime.now().minusHours(24);
		try {
			List<SourceFile> failedFiles = sourceFileRepository.findByProcessingStatus(FileProcessingStatus.ERROR);
			for (SourceFile file : failedFiles) {
				if (file.getUploadedAt().isBefore(threshold)) {
					log.info("Cleaning up failed file: {} ({})", file.getFileName(), file.getId());
				}
			}
		} catch (Exception ex) {
			log.error("Error cleaning up failed files: {}", ex.getMessage(), ex);
		}
	}
}