package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.response.DatasetRowsResponse;
import com.henri_fraise.hff_data_studio.enums.FileType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatasetRowReaderService {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	public DatasetRowsResponse readRows(String storagePath, FileType fileType, int page, int size) {
		if (storagePath == null || fileType == null) {
			return emptyResponse(page, size);
		}

		try {
			LoadedRows loaded =
					switch (fileType) {
						case CSV -> loadCsv(storagePath);
						case JSON -> loadJson(storagePath);
						case EXCEL, XLSX -> loadExcel(storagePath);
						default -> new LoadedRows(new ArrayList<>(), new ArrayList<>());
					};
			return paginate(loaded.columns(), loaded.rows(), page, size);
		} catch (Exception ex) {
			log.warn("Cannot read rows from {}: {}", storagePath, ex.getMessage());
			return emptyResponse(page, size);
		}
	}

	public List<Map<String, Object>> readAllRows(String storagePath, FileType fileType) {
		if (storagePath == null || fileType == null) {
			return new ArrayList<>();
		}
		try {
			return switch (fileType) {
				case CSV -> loadCsv(storagePath).rows();
				case JSON -> loadJson(storagePath).rows();
				case EXCEL, XLSX -> loadExcel(storagePath).rows();
				default -> new ArrayList<>();
			};
		} catch (Exception ex) {
			log.error("Cannot read all rows from {}: {}", storagePath, ex.getMessage());
			return new ArrayList<>();
		}
	}

	private record LoadedRows(List<String> columns, List<Map<String, Object>> rows) {}

	private LoadedRows loadCsv(String storagePath) throws IOException {
		Path path = Paths.get(storagePath);
		if (!Files.exists(path)) {
			return new LoadedRows(new ArrayList<>(), new ArrayList<>());
		}

		try (CSVParser parser =
				     CSVParser.parse(
						     path,
						     StandardCharsets.UTF_8,
						     CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

			List<String> columns = new ArrayList<>(parser.getHeaderNames());
			List<Map<String, Object>> rows = new ArrayList<>();

			for (CSVRecord record : parser) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (String column : columns) {
					row.put(column, record.isMapped(column) ? record.get(column) : null);
				}
				rows.add(row);
			}
			return new LoadedRows(columns, rows);
		}
	}

	private LoadedRows loadJson(String storagePath) throws IOException {
		Path path = Paths.get(storagePath);
		if (!Files.exists(path)) {
			return new LoadedRows(new ArrayList<>(), new ArrayList<>());
		}

		String content = Files.readString(path, StandardCharsets.UTF_8).trim();
		if (content.isEmpty()) {
			return new LoadedRows(new ArrayList<>(), new ArrayList<>());
		}

		JsonNode root = JSON_MAPPER.readTree(content);
		if (!root.isArray() || root.size() == 0) {
			return new LoadedRows(new ArrayList<>(), new ArrayList<>());
		}

		Set<String> keySet = new LinkedHashSet<>();
		for (JsonNode node : root) {
			if (node.isObject()) {
				node.fieldNames().forEachRemaining(keySet::add);
			}
		}
		List<String> columns = new ArrayList<>(keySet);

		List<Map<String, Object>> rows = new ArrayList<>();
		for (JsonNode node : root) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (String column : columns) {
				JsonNode value = node.get(column);
				row.put(column, value == null || value.isNull() ? null : value.asText());
			}
			rows.add(row);
		}
		return new LoadedRows(columns, rows);
	}

	private LoadedRows loadExcel(String storagePath) throws IOException {
		Path path = Paths.get(storagePath);
		if (!Files.exists(path)) {
			return new LoadedRows(new ArrayList<>(), new ArrayList<>());
		}

		try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
			if (workbook.getNumberOfSheets() == 0) {
				return new LoadedRows(new ArrayList<>(), new ArrayList<>());
			}

			Sheet sheet = workbook.getSheetAt(0);
			Row headerRow = sheet.getRow(0);
			if (headerRow == null) {
				return new LoadedRows(new ArrayList<>(), new ArrayList<>());
			}

			int columnCount = headerRow.getLastCellNum();
			List<String> columns = new ArrayList<>(columnCount);
			for (int c = 0; c < columnCount; c++) {
				Cell cell = headerRow.getCell(c);
				String name = cell != null ? getCellValue(cell) : "";
				if (name.isBlank()) {
					name = "Colonne " + (c + 1);
				}
				columns.add(name);
			}

			List<Map<String, Object>> rows = new ArrayList<>();
			int lastRow = sheet.getLastRowNum();
			for (int r = 1; r <= lastRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				Map<String, Object> rowData = new LinkedHashMap<>();
				for (int c = 0; c < columnCount; c++) {
					Cell cell = row.getCell(c);
					rowData.put(columns.get(c), cell != null ? getCellValue(cell) : null);
				}
				rows.add(rowData);
			}
			return new LoadedRows(columns, rows);
		}
	}

	private DatasetRowsResponse paginate(
			List<String> columns, List<Map<String, Object>> allRows, int page, int size) {

		int safePage = Math.max(0, page);
		int safeSize = Math.max(1, size);
		int totalRows = allRows.size();
		int fromIndex = Math.min(safePage * safeSize, totalRows);
		int toIndex = Math.min(fromIndex + safeSize, totalRows);

		List<Map<String, Object>> pageRows = new ArrayList<>(allRows.subList(fromIndex, toIndex));
		int totalPages = (int) Math.ceil((double) totalRows / safeSize);

		return DatasetRowsResponse.builder()
				.columns(columns)
				.rows(pageRows)
				.totalRows(totalRows)
				.pageNumber(safePage)
				.pageSize(safeSize)
				.totalPages(totalPages)
				.hasNext(safePage < totalPages - 1)
				.hasPrevious(safePage > 0)
				.build();
	}

	private DatasetRowsResponse emptyResponse(int page, int size) {
		return DatasetRowsResponse.builder()
				.columns(new ArrayList<>())
				.rows(new ArrayList<>())
				.totalRows(0)
				.pageNumber(page)
				.pageSize(size)
				.totalPages(0)
				.hasNext(false)
				.hasPrevious(false)
				.build();
	}

	private String getCellValue(Cell cell) {
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