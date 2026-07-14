package com.henri_fraise.hff_data_studio.validation.Handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
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

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(
			ConstraintViolationException ex,
			WebRequest request
	) {
		log.error("Constraint violation error: {}", ex.getMessage());

		Map<String, String> errors = ex.getConstraintViolations()
				.stream()
				.collect(Collectors.toMap(
						violation -> violation.getPropertyPath().toString(),
						ConstraintViolation::getMessage,
						(existing, replacement) -> existing
				));

		ValidationErrorResponse response = ValidationErrorResponse.builder()
				.status(HttpStatus.BAD_REQUEST.value())
				.error("Constraint Violation Error")
				.message("Invalid request parameters: " + errors)
				.path(request.getDescription(false).replaceFirst("uri=", ""))
				.validationErrors(errors)
				.build();
		return ResponseEntity.badRequest().body(response);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ValidationErrorResponse> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException ex,
			WebRequest request
	) {
		log.error("Missing request parameter error: {}", ex.getMessage());

		ValidationErrorResponse response = ValidationErrorResponse.builder()
					.status(HttpStatus.BAD_REQUEST.value())
					.error("Missing Request Parameter Error")
					.message("Missing request parameter: " + ex.getParameterName())
					.path(request.getDescription(false).replaceFirst("uri=", ""))
					.build();
		return ResponseEntity.badRequest().body(response);
	}
}
