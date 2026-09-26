package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.response.CleaningSuggestionResponse;
import com.henri_fraise.hff_data_studio.entity.Dataset;
import com.henri_fraise.hff_data_studio.entity.DatasetColumn;
import com.henri_fraise.hff_data_studio.enums.ColumnType;
import com.henri_fraise.hff_data_studio.repository.DatasetColumnRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleaningSuggestionService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern PHONE_PATTERN = Pattern.compile(
			"^[+]?[0-9][0-9\\-\\s().]{6,}$");
	private static final Pattern DATE_PATTERN = Pattern.compile(
			"^\\d{4}-\\d{2}-\\d{2}(T.*)?$|^\\d{2}/\\d{2}/\\d{4}$");

	private final DatasetColumnRepository columnRepository;
	private final DatasetService datasetService;

	@Transactional(readOnly = true)
	public List<CleaningSuggestionResponse> suggestForDataset(UUID datasetId) {
		Dataset dataset = datasetService.getDatasetEntityById(datasetId);
		List<DatasetColumn> columns = columnRepository.findByDatasetIdOrderByPositionAsc(datasetId);

		List<CleaningSuggestionResponse> responses = new ArrayList<>();
		for (DatasetColumn column : columns) {
			List<CleaningSuggestionResponse.Suggestion> suggestions =
					analyzeColumn(column, dataset.getRowCount());
			if (!suggestions.isEmpty()) {
				responses.add(CleaningSuggestionResponse.builder()
						.columnId(column.getId())
						.columnName(column.getOriginalName())
						.columnType(column.getDetectedType() != null
								? column.getDetectedType().name() : null)
						.suggestions(suggestions)
						.build());
			}
		}
		return responses;
	}

	private List<CleaningSuggestionResponse.Suggestion> analyzeColumn(
			DatasetColumn column, int rowCount) {
		List<CleaningSuggestionResponse.Suggestion> suggestions = new ArrayList<>();

		if (rowCount > 0 && column.getNullCount() > 0) {
			double nullRatio = (double) column.getNullCount() / rowCount;
			if (nullRatio > 0.05) {
				Map<String, Object> params = new HashMap<>();
				params.put("method", inferImputationMethod(column));
				suggestions.add(CleaningSuggestionResponse.Suggestion.builder()
						.ruleType("NULL_IMPUTATION")
						.priority(nullRatio > 0.3 ? 1 : 2)
						.reason(String.format("%.1f%% de valeurs nulles détectées", nullRatio * 100))
						.confidence(Math.min(0.95, nullRatio * 2 + 0.5))
						.parameters(params)
						.build());
			}
		}

		String name = column.getOriginalName() != null
				? column.getOriginalName().toLowerCase() : "";

		if (name.contains("email") || name.contains("mail")) {
			suggestions.add(CleaningSuggestionResponse.Suggestion.builder()
					.ruleType("REGEX_CLEANING")
					.priority(1)
					.reason("Colonne identifiée comme email")
					.confidence(0.9)
					.parameters(Map.of(
							"pattern", EMAIL_PATTERN.pattern(),
							"replacement", ""))
					.build());
		} else if (name.contains("phone") || name.contains("tel") || name.contains("mobile")) {
			suggestions.add(CleaningSuggestionResponse.Suggestion.builder()
					.ruleType("REGEX_CLEANING")
					.priority(1)
					.reason("Colonne identifiée comme numéro de téléphone")
					.confidence(0.85)
					.parameters(Map.of(
							"pattern", PHONE_PATTERN.pattern(),
							"replacement", ""))
					.build());
		} else if (name.contains("date") || name.contains("time")) {
			suggestions.add(CleaningSuggestionResponse.Suggestion.builder()
					.ruleType("REGEX_CLEANING")
					.priority(2)
					.reason("Colonne temporelle détectée")
					.confidence(0.8)
					.parameters(Map.of(
							"pattern", DATE_PATTERN.pattern(),
							"replacement", ""))
					.build());
		}

		if (column.getDetectedType() == ColumnType.STRING && name.matches(".*\\s+.*")) {
			suggestions.add(CleaningSuggestionResponse.Suggestion.builder()
					.ruleType("TRIM")
					.priority(3)
					.reason("Colonne de type texte avec espaces potentiels")
					.confidence(0.7)
					.parameters(Map.of())
					.build());
		}

		if (column.getDetectedType() == ColumnType.STRING
				&& rowCount > 0
				&& column.getUniqueCount() != null
				&& column.getUniqueCount() > 0
				&& column.getUniqueCount() < rowCount * 0.5) {
			suggestions.add(CleaningSuggestionResponse.Suggestion.builder()
					.ruleType("TYPE_CONVERSION")
					.priority(3)
					.reason("Colonne catégorielle détectée, conversion possible")
					.confidence(0.6)
					.parameters(Map.of("targetType", "STRING"))
					.build());
		}

		return suggestions;
	}

	private String inferImputationMethod(DatasetColumn column) {
		if (column.getDetectedType() == ColumnType.INTEGER
				|| column.getDetectedType() == ColumnType.FLOAT) {
			return "MEAN";
		}
		if (column.getDetectedType() == ColumnType.DATE) {
			return "FORWARD_FILL";
		}
		return "MODE";
	}
}