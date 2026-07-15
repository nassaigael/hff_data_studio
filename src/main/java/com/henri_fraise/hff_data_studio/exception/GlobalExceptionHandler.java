package com.henri_fraise.hff_data_studio.exception;

import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import com.henri_fraise.hff_data_studio.validation.Handler.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // ==================== Business Exceptions ====================

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {

    log.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.NOT_FOUND.value())
            .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(
      ResourceAlreadyExistsException ex, HttpServletRequest request) {

    log.warn("Resource already exists: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.CONFLICT.value())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(
      UnauthorizedException ex, HttpServletRequest request) {

    log.warn("Unauthorized: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(
      ForbiddenException ex, HttpServletRequest request) {

    log.warn("Forbidden: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.FORBIDDEN.value())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      InvalidCredentialsException ex, HttpServletRequest request) {

    log.warn("Invalid credentials: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler({TokenExpiredException.class, TokenInvalidException.class})
  public ResponseEntity<ErrorResponse> handleTokenException(
      BusinessException ex, HttpServletRequest request) {

    log.warn("Token error: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
      ValidationException ex, HttpServletRequest request) {

    log.warn("Validation error: {}", ex.getMessage());

    ValidationErrorResponse response =
        ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .errors(
                ex.getFieldErrors() != null
                    ? ex.getFieldErrors().entrySet().stream()
                        .map(
                            entry ->
                                ValidationErrorResponse.FieldError.builder()
                                    .field(entry.getKey())
                                    .message(entry.getValue())
                                    .build())
                        .collect(Collectors.toList())
                    : null)
            .validationErrors(ex.getFieldErrors())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler({
    FileProcessingException.class,
    AnalysisExecutionException.class,
    CleaningException.class,
    ExportException.class
  })
  public ResponseEntity<ErrorResponse> handleProcessingException(
      BusinessException ex, HttpServletRequest request) {

    log.error("Processing error: {}", ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .build();

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
      RateLimitExceededException ex, HttpServletRequest request) {

    log.warn("Rate limit exceeded: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .build();

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
  }

  @ExceptionHandler(DatabaseException.class)
  public ResponseEntity<ErrorResponse> handleDatabaseException(
      DatabaseException ex, HttpServletRequest request) {

    log.error("Database error: {}", ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message("A database error occurred. Please try again later.")
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalServiceException(
      ExternalServiceException ex, HttpServletRequest request) {

    log.error("External service error: {}", ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message("An external service error occurred. Please try again later.")
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .build();

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }

  // ==================== Spring Security Exceptions ====================

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {

    log.warn("Authentication error: {}", ex.getMessage());

    String errorCode =
        ex instanceof BadCredentialsException
            ? ErrorCode.INVALID_CREDENTIALS.getCode()
            : ErrorCode.UNAUTHORIZED.getCode();

    ErrorResponse response =
        ErrorResponse.builder()
            .code(errorCode)
            .message("Authentication failed: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {

    log.warn("Access denied: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.FORBIDDEN.getCode())
            .message("Access denied: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.FORBIDDEN.value())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  // ==================== Validation Exceptions ====================

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing));

    ValidationErrorResponse response =
        ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request parameters")
            .path(request.getRequestURI())
            .validationErrors(errors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(javax.validation.ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
      javax.validation.ConstraintViolationException ex, HttpServletRequest request) {

    log.warn("Constraint violation: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation -> {
              String fieldName = violation.getPropertyPath().toString();
              String message = violation.getMessage();
              errors.put(fieldName, message);
            });

    ValidationErrorResponse response =
        ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Constraint Violation")
            .message("Validation failed")
            .path(request.getRequestURI())
            .validationErrors(errors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
      MissingServletRequestParameterException ex, HttpServletRequest request) {

    log.warn("Missing parameter: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.VALIDATION_ERROR.getCode())
            .message("Required parameter missing: " + ex.getParameterName())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.BAD_REQUEST.value())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    log.warn("Message not readable: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.VALIDATION_ERROR.getCode())
            .message("Malformed request body: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.BAD_REQUEST.value())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // ==================== File Exceptions ====================

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {

    log.error("File size exceeded: {}", ex.getMessage());

    long maxSize = ex.getMaxUploadSize();
    String sizeStr = maxSize > 0 ? String.format("%.2f MB", maxSize / (1024.0 * 1024)) : "unknown";

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.MAX_FILE_SIZE_EXCEEDED.getCode())
            .message("File size exceeds maximum allowed limit of " + sizeStr)
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.PAYLOAD_TOO_LARGE.value())
            .build();

    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
  }

  // ==================== Database Exceptions ====================

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    log.error("Data integrity violation: {}", ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.DATABASE_ERROR.getCode())
            .message("Data integrity violation: Operation would violate database constraints")
            .details(ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.CONFLICT.value())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDataAccessException(
      DataAccessException ex, HttpServletRequest request) {

    log.error("Data access error: {}", ex.getMessage(), ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.DATABASE_ERROR.getCode())
            .message("A database access error occurred. Please try again later.")
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  // ==================== Generic Exceptions ====================

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {

    log.warn("Illegal argument: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.VALIDATION_ERROR.getCode())
            .message("Invalid argument: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.BAD_REQUEST.value())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(
      IllegalStateException ex, HttpServletRequest request) {

    log.warn("Illegal state: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
            .message("Invalid application state: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.CONFLICT.value())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(
      Exception ex, HttpServletRequest request) {

    log.error("Unexpected error: ", ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
            .message("An unexpected error occurred. Please try again later.")
            .details(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
