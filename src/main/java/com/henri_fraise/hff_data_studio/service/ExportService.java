package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.ExportRequest;
import com.henri_fraise.hff_data_studio.dto.response.ExportResponse;
import com.henri_fraise.hff_data_studio.entity.AnalysisExecution;
import com.henri_fraise.hff_data_studio.entity.AnalysisResult;
import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.mapper.ExportMapper;
import com.henri_fraise.hff_data_studio.repository.ExportRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

	private final ExportRepository exportRepository;
	private final ExportMapper exportMapper;
	private final AnalysisExecutionService executionService;
	private final AnalysisResultService resultService;
	private final UserService userService;
	private final SecurityUtils securityUtils;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	@Transactional
	public ExportResponse exportResults(ExportRequest request) {
		AnalysisExecution execution = executionService.getExecutionEntityById(request.getExecutionId());
		User user = userService.getUserEntityById(securityUtils.getCurrentUserId());

		String fileName = generateFileName(execution, request.getExportFormat());
		String filePath = generateFilePath(fileName);

		Export export = Export.builder()
				.exportFormat(request.getExportFormat())
				.filePath(filePath)
				.fileSize(0L)
				.execution(execution)
				.user(user)
				.build();

		Export saved = exportRepository.save(export);

		try {
			byte[] content = generateExportContent(request, execution, fileName);
			saveFile(filePath, content);

			saved.setFileSize((long) content.length);
			exportRepository.save(saved);

			log.info("Export created: {} for execution: {}", saved.getId(), execution.getId());
			return exportMapper.toResponse(saved);

		} catch (Exception e) {
			log.error("Error generating export: {}", e.getMessage(), e);
			exportRepository.delete(saved);
			throw new RuntimeException("Failed to generate export: " + e.getMessage());
		}
	}

	private byte[] generateExportContent(ExportRequest request, AnalysisExecution execution, String fileName) {
		ExportFormat format = request.getExportFormat();

		return switch (format) {
			case CSV -> generateCsvExport(execution, request.getResultIds());
			case XLSX -> generateExcelExport(execution, request.getResultIds());
			case PNG -> generateImageExport(execution, request.getResultIds());
			case ZIP -> generateZipExport(execution, request.getResultIds());
		};
	}

	private byte[] generateCsvExport(AnalysisExecution execution, List<UUID> resultIds) {
		List<AnalysisResult> results = getResults(execution, resultIds);
		StringBuilder csv = new StringBuilder();

		for (AnalysisResult result : results) {
			csv.append("Title: ").append(result.getTitle()).append("\n");
			csv.append("Type: ").append(result.getResultType()).append("\n");
			csv.append("Generated: ").append(execution.getExecutedAt()).append("\n\n");
		}

		return csv.toString().getBytes();
	}

	private byte[] generateExcelExport(AnalysisExecution execution, List<UUID> resultIds) {
		try {
			List<AnalysisResult> results = getResults(execution, resultIds);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			try {
				XSSFWorkbook workbook = new XSSFWorkbook();
				for (AnalysisResult result : results) {
					String sheetName = sanitizeSheetName(result.getTitle());
					XSSFSheet sheet = workbook.createSheet(sheetName);
					XSSFRow headerRow = sheet.createRow(0);
					headerRow.createCell(0).setCellValue("Title");
					headerRow.createCell(1).setCellValue(result.getTitle());
					headerRow.createCell(2).setCellValue("Type");
					headerRow.createCell(3).setCellValue(result.getResultType().name());
					headerRow.createCell(4).setCellValue("File Format");
					headerRow.createCell(5).setCellValue(result.getFileFormat().ordinal());
				}

				workbook.write(outputStream);
			} finally {

			}

			return outputStream.toByteArray();

		} catch (Exception e) {
			log.error("Error generating Excel export: {}", e.getMessage(), e);
			return "Error generating Excel export".getBytes();
		}
	}

	private byte[] generateImageExport(AnalysisExecution execution, List<UUID> resultIds) {
		List<AnalysisResult> results = getResults(execution, resultIds);
		if (results.isEmpty()) {
			return "No images available".getBytes();
		}

		try {
			AnalysisResult result = results.get(0);
			Path imagePath = Paths.get(result.getFilePath());
			if (Files.exists(imagePath)) {
				return Files.readAllBytes(imagePath);
			}
			return "Image file not found".getBytes();
		} catch (IOException e) {
			log.error("Error reading image: {}", e.getMessage());
			return "Error reading image".getBytes();
		}
	}

	private byte[] generateZipExport(AnalysisExecution execution, List<UUID> resultIds) {
		List<AnalysisResult> results = getResults(execution, resultIds);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			for (AnalysisResult result : results) {
				try {
					Path filePath = Paths.get(result.getFilePath());
					if (Files.exists(filePath)) {
						FileType fileFormat = result.getFileFormat() != null ?
								result.getFileFormat()  : FileType.PNG;
						String entryName = result.getTitle() + "." + fileFormat;
						ZipEntry zipEntry = new ZipEntry(entryName);
						zipOutputStream.putNextEntry(zipEntry);
						zipOutputStream.write(Files.readAllBytes(filePath));
						zipOutputStream.closeEntry();
					}
				} catch (IOException e) {
					log.warn("Could not add file to zip: {}", e.getMessage());
				}
			}

			String metadata = "Export generated at: " + LocalDateTime.now() + "\n";
			metadata += "Execution ID: " + execution.getId() + "\n";
			metadata += "Dataset: " + execution.getDataset().getDatasetName() + "\n";
			ZipEntry metadataEntry = new ZipEntry("metadata.txt");
			zipOutputStream.putNextEntry(metadataEntry);
			zipOutputStream.write(metadata.getBytes());
			zipOutputStream.closeEntry();

		} catch (IOException e) {
			log.error("Error generating zip: {}", e.getMessage());
			throw new RuntimeException("Failed to generate zip export");
		}

		return byteArrayOutputStream.toByteArray();
	}

	private List<AnalysisResult> getResults(AnalysisExecution execution, List<UUID> resultIds) {
		if (resultIds != null && !resultIds.isEmpty()) {
			return resultIds.stream()
					.map(resultService::getResultEntityById)
					.toList();
		}
		return resultService.getResultsByExecutionEntity(execution);
	}

	private void saveFile(String filePath, byte[] content) throws IOException {
		Path path = Paths.get(filePath);
		Files.createDirectories(path.getParent());
		Files.write(path, content);
	}

	private String generateFileName(AnalysisExecution execution, ExportFormat format) {
		String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
		String datasetName = execution.getDataset().getDatasetName().replaceAll("[^a-zA-Z0-9]", "_");
		String extension = getFileExtension(format);
		return String.format("export_%s_%s_%s.%s", datasetName, execution.getId().toString().substring(0, 8), timestamp, extension);
	}

	private String generateFilePath(String fileName) {
		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		return Paths.get("exports", datePath, fileName).toString();
	}

	private String getFileExtension(ExportFormat format) {
		return switch (format) {
			case CSV -> "csv";
			case XLSX -> "xlsx";
			case PNG -> "png";
			case ZIP -> "zip";
		};
	}

	private String sanitizeSheetName(String name) {
		if (name == null) return "Sheet";
		String sanitized = name.replaceAll("[^a-zA-Z0-9]", "_");
		return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
	}

	public Export getExportEntityById(UUID exportId) {
		return exportRepository.findById(exportId)
				.orElseThrow(() -> new ResourceNotFoundException("Export not found: " + exportId));
	}

	public ExportResponse getExportById(UUID exportId) {
		Export export = getExportEntityById(exportId);
		return exportMapper.toResponse(export);
	}

	public Page<ExportResponse> getExports(Pageable pageable, ExportFormat format) {
		Page<Export> exports;
		if (format != null) {
			exports = exportRepository.findByExportFormat(format, pageable);
		} else {
			exports = exportRepository.findAll(pageable);
		}
		return exports.map(exportMapper::toResponse);
	}

	public Page<ExportResponse> getExportsByExecution(UUID executionId, Pageable pageable) {
		Page<Export> exports = exportRepository.findByExecutionId(executionId, pageable);
		return exports.map(exportMapper::toResponse);
	}

	public List<ExportResponse> getExportsByUser(UUID userId) {
		List<Export> exports = exportRepository.findByUserIdOrderByExportedAtDesc(userId);
		return exports.stream().map(exportMapper::toResponse).toList();
	}

	public List<ExportResponse> getExportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		List<Export> exports = exportRepository.findByExportedAtBetween(startDate, endDate);
		return exports.stream().map(exportMapper::toResponse).toList();
	}

	public ResponseEntity<byte[]> downloadExport(UUID exportId) {
		Export export = getExportEntityById(exportId);
		Path filePath = Paths.get(export.getFilePath());

		try {
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				throw new RuntimeException("File not found: " + export.getFilePath());
			}

			String fileName = Paths.get(export.getFilePath()).getFileName().toString();
			String contentType = getContentType(export.getExportFormat());
			byte[] content = resource.getContentAsByteArray();

			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=\"" + fileName + "\"")
					.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length))
					.body(content);

		} catch (Exception e) {
			log.error("Error downloading export: {}", e.getMessage());
			throw new RuntimeException("Error downloading export: " + e.getMessage());
		}
	}

	public Resource downloadExportAsResource(UUID exportId) {
		Export export = getExportEntityById(exportId);
		Path filePath = Paths.get(export.getFilePath());

		try {
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists()) {
				throw new RuntimeException("File not found: " + export.getFilePath());
			}
			return resource;
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error accessing file", e);
		}
	}

	private String getContentType(ExportFormat format) {
		return switch (format) {
			case CSV -> "text/csv";
			case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case PNG -> "image/png";
			case ZIP -> "application/zip";
		};
	}

	@Transactional
	public void deleteExport(UUID exportId) {
		Export export = getExportEntityById(exportId);
		try {
			Path filePath = Paths.get(export.getFilePath());
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			log.warn("Could  not delete physical file: {}", e.getMessage());
		}
		exportRepository.delete(export);
		log.info("Export deleted: {}", exportId);
	}

	@Transactional
	public void deleteExportsByExecution(UUID executionId) {
		List<Export> exports = exportRepository.findByExecutionId(executionId);
		for (Export export : exports) {
			try {
				Path filePath = Paths.get(export.getFilePath());
				Files.deleteIfExists(filePath);
			} catch (IOException e) {
				log.warn("  Could not delete physical file: {}", e.getMessage());
			}
		}
		exportRepository.deleteAll(exports);
		log.info("Deleted {} exports for execution: {}", exports.size(), executionId);
	}

	@Transactional
	public void cleanupOldExports(LocalDateTime thresholdDate) {
		List<Export> oldExports = exportRepository.findByExportedAtBefore(thresholdDate);
		for (Export export : oldExports) {
			try {
				Path filePath = Paths.get(export.getFilePath());
				Files.deleteIfExists(filePath);
			} catch (IOException e) {
				log.warn("Could not delete physical file: {}", e.getMessage());
			}
		}
		exportRepository.deleteAll(oldExports);
		log.info("Cleaned up {} old exports", oldExports.size());
	}

	public long countExports() {
		return exportRepository.count();
	}

	public long countExportsByUser(UUID userId) {
		return exportRepository.countByUserId(userId);
	}

	public long countExportsByFormat(ExportFormat format) {
		return exportRepository.countByExportFormat(format);
	}

	public long countExportsByExecution(UUID executionId) {
		return exportRepository.countByExecutionId(executionId);
	}

	public long countExportsBetween(LocalDateTime startDate, LocalDateTime endDate) {
		return exportRepository.countByExportedAtBetween(startDate, endDate);
	}

	public long getTotalExportSize() {
		Long total = exportRepository.sumFileSizes();
		return total != null ? total : 0L;
	}

	public long getAverageExportSize() {
		Double avg = exportRepository.averageFileSize();
		return avg != null ? avg.longValue() : 0L;
	}

	public long getTotalExportSizeByUser(UUID userId) {
		Long total = exportRepository.sumFileSizesByUserId(userId);
		return total != null ? total : 0L;
	}

	public List<Object[]> countByFormatGrouped() {
		return exportRepository.countGroupByFormat();
	}

	public List<Object[]> countByUserGrouped() {
		return exportRepository.countGroupByUser();
	}

	public Map<String, Object> getExportStatistics(UUID exportId) {
		Export export = getExportEntityById(exportId);
		Map<String, Object> stats = new HashMap<>();
		stats.put("exportId", export.getId());
		stats.put("format", export.getExportFormat());
		stats.put("fileSize", export.getFileSize());
		stats.put("fileSizeFormatted", formatFileSize(export.getFileSize()));
		stats.put("exportedAt", export.getExportedAt());
		stats.put("executionId", export.getExecution() != null ? export.getExecution().getId() : null);
		stats.put("executionStatus", export.getExecution() != null ? export.getExecution().getStatus() : null);
		stats.put("userId", export.getUser() != null ? export.getUser().getId() : null);
		stats.put("userFullName", export.getUser() != null
				? export.getUser().getFirstName() + " " + export.getUser().getLastName()
				: null);
		stats.put("downloadUrl", "/api/v1/exports/" + export.getId() + "/download");
		return stats;
	}

	public Map<String, Object> getGlobalStatistics() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalExports", countExports());
		stats.put("totalSize", formatFileSize(getTotalExportSize()));
		stats.put("averageSize", formatFileSize(getAverageExportSize()));
		stats.put("totalCsv", countExportsByFormat(ExportFormat.CSV));
		stats.put("totalExcel", countExportsByFormat(ExportFormat.XLSX));
		stats.put("totalPng", countExportsByFormat(ExportFormat.PNG));
		stats.put("totalZip", countExportsByFormat(ExportFormat.ZIP));
		return stats;
	}

	private String formatFileSize(Long bytes) {
		if (bytes == null || bytes == 0) return "0 B";
		if (bytes < 1024) return bytes + " B";
		if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
		if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
		return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
	}
}