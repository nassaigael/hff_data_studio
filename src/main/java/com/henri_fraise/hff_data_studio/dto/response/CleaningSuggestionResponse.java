package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningSuggestionResponse {
	private UUID columnId;
	private String columnName;
	private String columnType;
	private List<Suggestion> suggestions;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Suggestion {
		private String ruleType;
		private int priority;
		private String reason;
		private double confidence;
		private Map<String, Object> parameters;
	}
}