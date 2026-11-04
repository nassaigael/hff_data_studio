package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningRequest {

	@NotEmpty(message = "At least one rule is required")
	private List<RuleDto> rules;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RuleDto {
		@NotNull(message = "Column ID is required")
		private UUID columnId;

		@NotBlank(message = "Rule type is required")
		private String ruleType;

		private String value;
	}
}