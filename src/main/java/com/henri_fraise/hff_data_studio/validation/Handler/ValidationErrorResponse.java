package com.henri_fraise.hff_data_studio.validation.Handler;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	private Integer status;

	private String error;

	private String message;

	private String path;

	private List<FieldError> errors;

	private Map<String, String> validationErrors;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FieldError {
		private String field;
		private String rejectedValue;
		private String message;
	}
}
