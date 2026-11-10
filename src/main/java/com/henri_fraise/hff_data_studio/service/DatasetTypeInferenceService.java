package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.repository.DatasetColumnRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatasetTypeInferenceService {

	private static final int SAMPLE_SIZE = 500;
	private static final int MAX_JSON_KEYS = 50;

	private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
	private static final Pattern FLOAT_PATTERN = Pattern.compile("^-?\\d+([.,]\\d+)?$");
	private static final Pattern BOOLEAN_PATTERN =
			Pattern.compile("^(true|false|oui|non|yes|no)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern DATE_PATTERN_ISO =
			Pattern.compile("^\\d{4}-\\d{2}-\\d{2}([ T].*)?$");
	private static final Pattern DATE_PATTERN_FR = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");
	private static final Pattern JSON_KEY_PATTERN =
			Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|[^,}\\]]+)");

	private final DatasetColumnRepository datasetColumnRepository;

	@Transactional
	public void inferAndSaveColumns(Dataset dataset, String storagePath, FileType fileType) {
		if (dataset == null || storagePath == null || fileType == null) {
			return;
		}

		List<ColumnSample> samples = extractSamples(storagePath, fileType);
		if (samples.isEmpty()) {
			log.debug("No column samples extracted for dataset {}", dataset.getId());
			return;
		}

		List<DatasetColumn> columns = new ArrayList<>();
		for (int i = 0; i < samples.size(); i++) {
			ColumnSample sample = samples.get(i);
			ColumnType type = inferType(sample.values());
			int nullCount = countNulls(sample.values());
			int uniqueCount = countUniques(sample.values());

			DatasetColumn column =
					DatasetColumn.builder()
							.dataset(dataset)
							.originalName(sample.name())
							.normalizedName(normalize(sample.name()))
							.detectedType(type)
							.targetType(type)
							.position(i)
							.nullCount(nullCount)
							.uniqueCount(uniqueCount)
							.build();

			columns.add(column);
		}

		datasetColumnRepository.saveAll(columns);
		log.info("Inferred {} columns for dataset {}", columns.size(), dataset.getId());
	}

	private record ColumnSample(String name, List<String> values) {}

	private List<ColumnSample> extractSamples(String storagePath, FileType fileType) {
		if (fileType == FileType.CSV) {
			return extractCsvSamples(storagePath);
		}
		if (fileType == FileType.EXCEL || fileType == FileType.XLSX) {
			return extractExcelSamples(storagePath);
		}
		if (fileType == FileType.JSON) {
			return extractJsonSamples(storagePath);
		}
		return List.of();
	}

	private List<ColumnSample> extractCsvSamples(String storagePath) {
		try {
			Path path = Paths.get(storagePath);
			if (!Files.exists(path)) {
				return List.of();
			}

			List<String> lines =
					Files.readAllLines(path, StandardCharsets.UTF_8).stream()
							.filter(line -> !line.isBlank())
							.limit(SAMPLE_SIZE + 1L)
							.toList();

			if (lines.isEmpty()) {
				return List.of();
			}

			List<String> headers = splitCsvLine(lines.getFirst());
			List<ColumnSample> samples = new ArrayList<>();
			for (String header : headers) {
				samples.add(new ColumnSample(header, new ArrayList<>()));
			}

			for (int i = 1; i < lines.size(); i++) {
				List<String> values = splitCsvLine(lines.get(i));
				for (int c = 0; c < samples.size() && c < values.size(); c++) {
					samples.get(c).values().add(values.get(c));
				}
			}

			return samples;
		} catch (IOException ex) {
			log.warn("Cannot read CSV for type inference: {}", storagePath, ex);
			return List.of();
		}
	}

	private List<String> splitCsvLine(String line) {
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				result.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		result.add(current.toString().trim());
		return result;
	}

	private List<ColumnSample> extractExcelSamples(String storagePath) {
		try {
			Path path = Paths.get(storagePath);
			if (!Files.exists(path)) {
				return List.of();
			}

			try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
				if (workbook.getNumberOfSheets() == 0) {
					return List.of();
				}

				Sheet bestSheet = null;
				int bestRowCount = 0;
				for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
					Sheet sheet = workbook.getSheetAt(s);
					int rowCount = sheet.getPhysicalNumberOfRows();
					if (rowCount > bestRowCount) {
						bestRowCount = rowCount;
						bestSheet = sheet;
					}
				}

				if (bestSheet == null) {
					return List.of();
				}

				Row headerRow = bestSheet.getRow(0);
				if (headerRow == null) {
					return List.of();
				}

				int colCount = headerRow.getLastCellNum();
				List<ColumnSample> samples = new ArrayList<>();
				for (int c = 0; c < colCount; c++) {
					Cell cell = headerRow.getCell(c);
					String name = cell != null ? getCellValueAsString(cell) : "";
					if (name.isBlank()) {
						name = "Colonne " + (c + 1);
					}
					samples.add(new ColumnSample(name, new ArrayList<>()));
				}

				int lastRow = Math.min(bestSheet.getLastRowNum(), SAMPLE_SIZE);
				for (int r = 1; r <= lastRow; r++) {
					Row row = bestSheet.getRow(r);
					if (row == null) {
						continue;
					}
					for (int c = 0; c < samples.size(); c++) {
						Cell cell = row.getCell(c);
						samples.get(c).values().add(cell != null ? getCellValueAsString(cell) : "");
					}
				}

				return samples;
			}
		} catch (Exception ex) {
			log.warn("Cannot read Excel for type inference: {}", storagePath, ex);
			return List.of();
		}
	}

	private List<ColumnSample> extractJsonSamples(String storagePath) {
		try {
			Path path = Paths.get(storagePath);
			if (!Files.exists(path)) {
				return List.of();
			}

			String content = Files.readString(path, StandardCharsets.UTF_8).trim();
			if (!content.startsWith("{") && !content.startsWith("[")) {
				return List.of();
			}

			Matcher matcher = JSON_KEY_PATTERN.matcher(content);
			List<ColumnSample> samples = new ArrayList<>();
			Set<String> seen = new HashSet<>();

			while (matcher.find() && samples.size() < MAX_JSON_KEYS) {
				String key = matcher.group(1);
				String value = matcher.group(2).replace("\"", "").trim();
				if (seen.add(key)) {
					ColumnSample sample = new ColumnSample(key, new ArrayList<>());
					sample.values().add(value);
					samples.add(sample);
				}
			}

			return samples;
		} catch (Exception ex) {
			log.warn("Cannot read JSON for type inference: {}", storagePath, ex);
			return List.of();
		}
	}

	private ColumnType inferType(List<String> values) {
		List<String> nonNull =
				values.stream().filter(v -> v != null && !v.isBlank()).toList();

		if (nonNull.isEmpty()) {
			return ColumnType.STRING;
		}

		if (nonNull.stream()
				.allMatch(
						v -> DATE_PATTERN_ISO.matcher(v).matches() || DATE_PATTERN_FR.matcher(v).matches())) {
			return ColumnType.DATE;
		}

		if (nonNull.stream().allMatch(v -> BOOLEAN_PATTERN.matcher(v).matches())) {
			return ColumnType.BOOLEAN;
		}

		if (nonNull.stream().allMatch(v -> INTEGER_PATTERN.matcher(v).matches())) {
			return ColumnType.INTEGER;
		}

		if (nonNull.stream().allMatch(v -> FLOAT_PATTERN.matcher(v).matches())) {
			return ColumnType.FLOAT;
		}

		return ColumnType.STRING;
	}

	private int countNulls(List<String> values) {
		return (int) values.stream().filter(v -> v == null || v.isBlank()).count();
	}

	private int countUniques(List<String> values) {
		return (int) values.stream().filter(v -> v != null && !v.isBlank()).distinct().count();
	}

	private String normalize(String name) {
		if (name == null) {
			return "colonne";
		}
		return name.toLowerCase()
				.trim()
				.replaceAll("[^a-z0-9]+", "_")
				.replaceAll("^_+|_+$", "");
	}

	private String getCellValueAsString(Cell cell) {
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue();
			case NUMERIC -> {
				if (DateUtil.isCellDateFormatted(cell)) {
					yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
				}
				yield String.valueOf(cell.getNumericCellValue());
			}
			case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
			case FORMULA -> cell.getCellFormula();
			default -> "";
		};
	}
}