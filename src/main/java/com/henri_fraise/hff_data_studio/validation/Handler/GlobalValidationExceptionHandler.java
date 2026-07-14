package com.henri_fraise.hff_data_studio.validation.Handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalValidationExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
			MethodArgumentNotValidException ex,
			WebRequest request
	) {
		log.error("Validation error: {}", ex.getMessage());

		Map<String, String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						FieldError::getDefaultMessage,
						(existing, replacement) -> existing
				));

		ValidationErrorResponse response = ValidationErrorResponse.builder()
				.status(HttpStatus.BAD_REQUEST.value())
				.error("Validation Error")
				.message("Invalid request parameters: " + errors)
				.path(request.getDescription(false).replaceFirst("uri=", ""))
				.validationErrors(errors)
				.build();
		return ResponseEntity.badRequest().body(response);
	}
}
