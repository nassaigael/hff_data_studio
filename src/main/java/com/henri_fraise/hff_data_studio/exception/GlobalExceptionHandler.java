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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidProjectIdException.class)
  public ResponseEntity<ErrorResponse> handleInvalidProjectId(
          InvalidProjectIdException ex, HttpServletRequest request) {
    log.warn("Invalid project id: {}", ex.getMessage());
    return buildError(
            ErrorCode.VALIDATION_ERROR.getCode(),
            ex.getMessage(),
            ex.getValue(),
            HttpStatus.BAD_REQUEST,
            request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
          MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getValue());
    String detail = "Parameter '%s' has invalid value '%s'"
            .formatted(ex.getName(), String.valueOf(ex.getValue()));
    return buildError(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "Invalid parameter value",
            detail,
            HttpStatus.BAD_REQUEST,
            request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
          MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing));

    ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request parameters")
            .path(request.getRequestURI())
            .validationErrors(errors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
          jakarta.validation.ConstraintViolationException ex, HttpServletRequest request) {
    log.warn("Constraint violation: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation -> errors.put(
            violation.getPropertyPath().toString(),
            violation.getMessage()));

    ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Constraint Violation")
            .message("Validation failed")
            .path(request.getRequestURI())
            .validationErrors(errors)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
          ValidationException ex, HttpServletRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .errors(ex.getFieldErrors() != null
                    ? ex.getFieldErrors().entrySet().stream()
                    .map(entry -> ValidationErrorResponse.FieldError.builder()
                            .field(entry.getKey())
                            .message(entry.getValue())
                            .build())
                    .collect(Collectors.toList())
                    : null)
            .validationErrors(ex.getFieldErrors())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(
          MissingServletRequestParameterException ex, HttpServletRequest request) {
    log.warn("Missing parameter: {}", ex.getMessage());
    return buildError(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "Required parameter missing: " + ex.getParameterName(),
            null,
            HttpStatus.BAD_REQUEST,
            request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMessageNotReadable(
          HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn("Message not readable: {}", ex.getMessage());
    return buildError(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "Malformed request body",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST,
            request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
          IllegalArgumentException ex, HttpServletRequest request) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return buildError(
            ErrorCode.VALIDATION_ERROR.getCode(),
            "Invalid argument: " + ex.getMessage(),
            null,
            HttpStatus.BAD_REQUEST,
            request);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(
          IllegalStateException ex, HttpServletRequest request) {
    log.warn("Illegal state: {}", ex.getMessage());
    return buildError(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "Invalid application state: " + ex.getMessage(),
            null,
            HttpStatus.CONFLICT,
            request);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(
          ResourceNotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.NOT_FOUND,
            request);
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(
          ResourceAlreadyExistsException ex, HttpServletRequest request) {
    log.warn("Resource already exists: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.CONFLICT,
            request);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(
          UnauthorizedException ex, HttpServletRequest request) {
    log.warn("Unauthorized: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.UNAUTHORIZED,
            request);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(
          ForbiddenException ex, HttpServletRequest request) {
    log.warn("Forbidden: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.FORBIDDEN,
            request);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
          InvalidCredentialsException ex, HttpServletRequest request) {
    log.warn("Invalid credentials: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.UNAUTHORIZED,
            request);
  }

  @ExceptionHandler({TokenExpiredException.class, TokenInvalidException.class})
  public ResponseEntity<ErrorResponse> handleTokenException(
          BusinessException ex, HttpServletRequest request) {
    log.warn("Token error: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.UNAUTHORIZED,
            request);
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
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.UNPROCESSABLE_ENTITY,
            request);
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
          RateLimitExceededException ex, HttpServletRequest request) {
    log.warn("Rate limit exceeded: {}", ex.getMessage());
    return buildError(
            ex.getErrorCode(),
            ex.getMessage(),
            null,
            HttpStatus.TOO_MANY_REQUESTS,
            request);
  }

  @ExceptionHandler(DatabaseException.class)
  public ResponseEntity<ErrorResponse> handleDatabaseException(
          DatabaseException ex, HttpServletRequest request) {
    log.error("Database error: {}", ex.getMessage(), ex);
    return buildError(
            ex.getErrorCode(),
            "A database error occurred. Please try again later.",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            request);
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalServiceException(
          ExternalServiceException ex, HttpServletRequest request) {
    log.error("External service error: {}", ex.getMessage(), ex);
    return buildError(
            ex.getErrorCode(),
            "An external service error occurred. Please try again later.",
            ex.getMessage(),
            HttpStatus.SERVICE_UNAVAILABLE,
            request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
          DataIntegrityViolationException ex, HttpServletRequest request) {
    log.error("Data integrity violation: {}", ex.getMessage(), ex);
    String detail = ex.getRootCause() != null
            ? ex.getRootCause().getMessage()
            : ex.getMessage();
    return buildError(
            ErrorCode.DATABASE_ERROR.getCode(),
            "Data integrity violation: operation would violate database constraints",
            detail,
            HttpStatus.CONFLICT,
            request);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDataAccessException(
          DataAccessException ex, HttpServletRequest request) {
    log.error("Data access error: {}", ex.getMessage(), ex);
    return buildError(
            ErrorCode.DATABASE_ERROR.getCode(),
            "A database access error occurred. Please try again later.",
            ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            request);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
          MaxUploadSizeExceededException ex, HttpServletRequest request) {
    log.error("File size exceeded: {}", ex.getMessage());

    long maxSize = ex.getMaxUploadSize();
    String sizeStr = maxSize > 0
            ? String.format("%.2f MB", maxSize / (1024.0 * 1024))
            : "unknown";

    return buildError(
            ErrorCode.MAX_FILE_SIZE_EXCEEDED.getCode(),
            "File size exceeds maximum allowed limit of " + sizeStr,
            ex.getMessage(),
            HttpStatus.PAYLOAD_TOO_LARGE,
            request);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(
          BadCredentialsException ex, HttpServletRequest request) {
    log.warn("Bad credentials: {}", ex.getMessage());
    return buildError(
            ErrorCode.INVALID_CREDENTIALS.getCode(),
            "Authentication failed",
            null,
            HttpStatus.UNAUTHORIZED,
            request);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
          AuthenticationException ex, HttpServletRequest request) {
    log.warn("Authentication error: {}", ex.getMessage());
    return buildError(
            ErrorCode.UNAUTHORIZED.getCode(),
            "Authentication failed",
            null,
            HttpStatus.UNAUTHORIZED,
            request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
          AccessDeniedException ex, HttpServletRequest request) {
    log.warn("Access denied: {}", ex.getMessage());
    return buildError(
            ErrorCode.FORBIDDEN.getCode(),
            "Access denied",
            null,
            HttpStatus.FORBIDDEN,
            request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllExceptions(
          Exception ex, HttpServletRequest request) {
    log.error("Unexpected error on {} {}",
            request.getMethod(), request.getRequestURI(), ex);
    return buildError(
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            "An unexpected error occurred. Please try again later.",
            null,
            HttpStatus.INTERNAL_SERVER_ERROR,
            request);
  }

  private ResponseEntity<ErrorResponse> buildError(
          String code,
          String message,
          String details,
          HttpStatus status,
          HttpServletRequest request) {

    ErrorResponse response = ErrorResponse.builder()
            .code(code)
            .message(message)
            .details(details)
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(status.value())
            .build();

    return ResponseEntity.status(status).body(response);
  }
}