package com.henri_fraise.hff_data_studio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.henri_fraise.hff_data_studio.enums.FileType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatasetRowWriterService {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	public void writeRows(String storagePath, FileType fileType, List<String> columns, List<Map<String, Object>> rows) {
		if (storagePath == null || fileType == null || rows == null) {
			return;
		}

		try {
			switch (fileType) {
				case CSV -> writeCsv(storagePath, columns, rows);
				case JSON -> writeJson(storagePath, rows);
				case EXCEL, XLSX -> writeExcel(storagePath, columns, rows);
				default -> log.warn("Writing not supported for file type: {}", fileType);
			}
			log.info("Successfully wrote {} rows to {}", rows.size(), storagePath);
		} catch (Exception e) {
			log.error("Failed to write data to file: {}", storagePath, e);
			throw new RuntimeException("Failed to write cleaned data", e);
		}
	}

	private void writeCsv(String storagePath, List<String> columns, List<Map<String, Object>> rows) throws IOException {
		Path path = Paths.get(storagePath);
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
		     CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
				     .setHeader(columns.toArray(new String[0]))
				     .build())) {
			for (Map<String, Object> row : rows) {
				printer.printRecord(columns.stream().map(c -> row.getOrDefault(c, "")).toList());
			}
		}
	}

	private void writeJson(String storagePath, List<Map<String, Object>> rows) throws IOException {
		Path path = Paths.get(storagePath);
		ArrayNode arrayNode = JSON_MAPPER.createArrayNode();

		for (Map<String, Object> row : rows) {
			ObjectNode objectNode = JSON_MAPPER.createObjectNode();
			row.forEach((k, v) -> {
				if (v == null) {
					objectNode.putNull(k);
				} else if (v instanceof Integer) {
					objectNode.put(k, (Integer) v);
				} else if (v instanceof Double) {
					objectNode.put(k, (Double) v);
				} else if (v instanceof Boolean) {
					objectNode.put(k, (Boolean) v);
				} else {
					objectNode.put(k, v.toString());
				}
			});
			arrayNode.add(objectNode);
		}

		JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), arrayNode);
	}

	private void writeExcel(String storagePath, List<String> columns, List<Map<String, Object>> rows) throws IOException {
		Path path = Paths.get(storagePath);
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Data");

			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < columns.size(); i++) {
				headerRow.createCell(i).setCellValue(columns.get(i));
			}

			int rowIndex = 1;
			for (Map<String, Object> row : rows) {
				Row excelRow = sheet.createRow(rowIndex++);
				for (int i = 0; i < columns.size(); i++) {
					Cell cell = excelRow.createCell(i);
					Object value = row.get(columns.get(i));
					if (value != null) {
						if (value instanceof Number n) {
							cell.setCellValue(n.doubleValue());
						} else if (value instanceof Boolean b) {
							cell.setCellValue(b);
						} else {
							cell.setCellValue(value.toString());
						}
					}
				}
			}

			try (var out = Files.newOutputStream(path)) {
				workbook.write(out);
			}
		}
	}
}