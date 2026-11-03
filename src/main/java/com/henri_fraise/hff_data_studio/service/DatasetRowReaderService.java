package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.response.DatasetRowsResponse;
import com.henri_fraise.hff_data_studio.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class DatasetRowReaderService {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	public DatasetRowsResponse readRows(
			String storagePath, FileType fileType, int page, int size) {

		if (storagePath == null || fileType == null) {
			return emptyResponse(page, size);
		}

		try {
			return switch (fileType) {
				case CSV -> readCsv(storagePath, page, size);
				case JSON -> readJson(storagePath, page, size);
				case EXCEL, XLSX -> readExcel(storagePath, page, size);
				default -> emptyResponse(page, size);
			};
		} catch (Exception ex) {
			log.warn("Cannot read rows from {}: {}", storagePath, ex.getMessage());
			return emptyResponse(page, size);
		}
	}

	private DatasetRowsResponse readCsv(String storagePath, int page, int size) throws IOException {
		Path path = Paths.get(storagePath);
		if (!Files.exists(path)) {
			return emptyResponse(page, size);
		}

		try (CSVParser parser =
				     CSVParser.parse(
						     path,
						     StandardCharsets.UTF_8,
						     CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

			List<String> columns = new ArrayList<>(parser.getHeaderNames());
			List<Map<String, Object>> allRows = new ArrayList<>();

			for (CSVRecord record : parser) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (String col : columns) {
					row.put(col, record.isMapped(col) ? record.get(col) : null);
				}
				allRows.add(row);
			}

			return paginate(columns, allRows, page, size);
		}
	}

	private DatasetRowsResponse readJson(String storagePath, int page, int size) throws IOException {
		Path path = Paths.get(storagePath);
		if (!Files.exists(path)) {
			return emptyResponse(page, size);
		}

		String content = Files.readString(path, StandardCharsets.UTF_8).trim();
		if (content.isEmpty()) {
			return emptyResponse(page, size);
		}

		JsonNode root = JSON_MAPPER.readTree(content);

		if (root.isArray()) {
			if (root.isEmpty()) {
				return emptyResponse(page, size);
			}

			Set<String> keys = new LinkedHashSet<>();
			for (JsonNode node : root) {
				if (node.isObject()) {
					node.fieldNames().forEachRemaining(keys::add);
				}
			}

			List<String> columns = new ArrayList<>(keys);
			List<Map<String, Object>> allRows = new ArrayList<>();

			for (JsonNode node : root) {
				Map<String, Object> row = new LinkedHashMap<>();
				for (String col : columns) {
					JsonNode value = node.get(col);
					row.put(col, value == null || value.isNull() ? null : value.asText());
				}
				allRows.add(row);
			}

			return paginate(columns, allRows, page, size);
		}

		if (root.isObject()) {
			List<String> columns = new ArrayList<>();
			Map<String, Object> row = new LinkedHashMap<>();
			root.fields()
					.forEachRemaining(
							entry -> {
								columns.add(entry.getKey());
								row.put(entry.getKey(), entry.getValue().asText());
							});
			return paginate(columns, List.of(row), page, size);
		}

		return emptyResponse(page, size);
	}

	private DatasetRowsResponse readExcel(String storagePath, int page, int size) throws IOException {
		Path path = Paths.get(storagePath);
		if (!Files.exists(path)) {
			return emptyResponse(page, size);
		}

		try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
			if (workbook.getNumberOfSheets() == 0) {
				return emptyResponse(page, size);
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
				return emptyResponse(page, size);
			}

			Row headerRow = bestSheet.getRow(0);
			if (headerRow == null) {
				return emptyResponse(page, size);
			}

			int colCount = headerRow.getLastCellNum();
			List<String> columns = new ArrayList<>();
			for (int c = 0; c < colCount; c++) {
				Cell cell = headerRow.getCell(c);
				String name = cell != null ? getCellValue(cell) : "";
				if (name.isBlank()) {
					name = "Colonne " + (c + 1);
				}
				columns.add(name);
			}

			List<Map<String, Object>> allRows = new ArrayList<>();
			int lastRow = bestSheet.getLastRowNum();

			for (int r = 1; r <= lastRow; r++) {
				Row row = bestSheet.getRow(r);
				if (row == null) continue;

				boolean isEmpty = true;
				Map<String, Object> mapRow = new LinkedHashMap<>();
				for (int c = 0; c < columns.size(); c++) {
					Cell cell = row.getCell(c);
					String value = cell != null ? getCellValue(cell) : null;
					if (value != null && !value.isBlank()) isEmpty = false;
					mapRow.put(columns.get(c), value);
				}
				if (!isEmpty) {
					allRows.add(mapRow);
				}
			}

			return paginate(columns, allRows, page, size);
		}
	}

	private DatasetRowsResponse paginate(
			List<String> columns, List<Map<String, Object>> allRows, int page, int size) {

		int safePage = Math.max(0, page);
		int safeSize = Math.max(1, size);
		int totalRows = allRows.size();
		int fromIndex = Math.min(safePage * safeSize, totalRows);
		int toIndex = Math.min(fromIndex + safeSize, totalRows);

		List<Map<String, Object>> pageRows = allRows.subList(fromIndex, toIndex);
		int totalPages = (int) Math.ceil((double) totalRows / safeSize);

		return DatasetRowsResponse.builder()
				.columns(columns)
				.rows(new ArrayList<>(pageRows))
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
				.columns(List.of())
				.rows(List.of())
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
				if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
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