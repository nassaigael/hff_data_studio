Parfait ! Passons maintenant au package `exception` avec des exceptions personnalisées et un gestionnaire global.

---

## Structure du package

```
com.henri_fraise.hff_data_studio.exception
├── BusinessException.java
├── ResourceNotFoundException.java
├── ResourceAlreadyExistsException.java
├── UnauthorizedException.java
├── ForbiddenException.java
├── ValidationException.java
├── FileProcessingException.java
├── AnalysisExecutionException.java
├── CleaningException.java
├── ExportException.java
├── InvalidCredentialsException.java
├── TokenExpiredException.java
├── TokenInvalidException.java
├── RateLimitExceededException.java
├── DatabaseException.java
├── ExternalServiceException.java
├── ErrorCode.java
└── GlobalExceptionHandler.java
```

---

## 1. Exception de base

### BusinessException.java

```java
package com.henri_fraise.hff_data_studio.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] args;

    protected BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    protected BusinessException(String message, String errorCode, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    protected BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
}
```

---

## 2. Exceptions spécifiques

### ResourceNotFoundException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class ResourceNotFoundException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ResourceNotFoundException(String entityName, Object id) {
        super(
            String.format("%s not found with id: %s", entityName, id),
            DEFAULT_ERROR_CODE,
            entityName, id
        );
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }

    public ResourceNotFoundException(String entityName, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s: %s", entityName, fieldName, fieldValue),
            DEFAULT_ERROR_CODE,
            entityName, fieldName, fieldValue
        );
    }
}
```

---

### ResourceAlreadyExistsException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class ResourceAlreadyExistsException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_ALREADY_EXISTS";

    public ResourceAlreadyExistsException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ResourceAlreadyExistsException(String entityName, String fieldName, Object fieldValue) {
        super(
            String.format("%s already exists with %s: %s", entityName, fieldName, fieldValue),
            DEFAULT_ERROR_CODE,
            entityName, fieldName, fieldValue
        );
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### UnauthorizedException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class UnauthorizedException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public UnauthorizedException() {
        super("Authentication required to access this resource", DEFAULT_ERROR_CODE);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### ForbiddenException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class ForbiddenException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "FORBIDDEN";

    public ForbiddenException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ForbiddenException() {
        super("You don't have permission to access this resource", DEFAULT_ERROR_CODE);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### InvalidCredentialsException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class InvalidCredentialsException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_CREDENTIALS";

    public InvalidCredentialsException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public InvalidCredentialsException() {
        super("Invalid email or password", DEFAULT_ERROR_CODE);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### TokenExpiredException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class TokenExpiredException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "TOKEN_EXPIRED";

    public TokenExpiredException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public TokenExpiredException() {
        super("Authentication token has expired. Please login again", DEFAULT_ERROR_CODE);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### TokenInvalidException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class TokenInvalidException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "TOKEN_INVALID";

    public TokenInvalidException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public TokenInvalidException() {
        super("Invalid authentication token", DEFAULT_ERROR_CODE);
    }

    public TokenInvalidException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### ValidationException.java

```java
package com.henri_fraise.hff_data_studio.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ValidationException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";
    private final Map<String, String> fieldErrors;
    private final List<String> globalErrors;

    public ValidationException(String message) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldErrors = null;
        this.globalErrors = null;
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldErrors = fieldErrors;
        this.globalErrors = null;
    }

    public ValidationException(String message, List<String> globalErrors) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldErrors = null;
        this.globalErrors = globalErrors;
    }

    public ValidationException(String message, Map<String, String> fieldErrors, List<String> globalErrors) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldErrors = fieldErrors;
        this.globalErrors = globalErrors;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
        this.fieldErrors = null;
        this.globalErrors = null;
    }
}
```

---

### FileProcessingException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class FileProcessingException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "FILE_PROCESSING_ERROR";

    public FileProcessingException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public FileProcessingException(String fileName, String reason) {
        super(
            String.format("Error processing file '%s': %s", fileName, reason),
            DEFAULT_ERROR_CODE,
            fileName, reason
        );
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### AnalysisExecutionException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class AnalysisExecutionException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "ANALYSIS_EXECUTION_ERROR";

    public AnalysisExecutionException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public AnalysisExecutionException(String analysisName, String reason) {
        super(
            String.format("Error executing analysis '%s': %s", analysisName, reason),
            DEFAULT_ERROR_CODE,
            analysisName, reason
        );
    }

    public AnalysisExecutionException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### CleaningException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class CleaningException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "CLEANING_ERROR";

    public CleaningException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public CleaningException(String datasetName, String reason) {
        super(
            String.format("Error cleaning dataset '%s': %s", datasetName, reason),
            DEFAULT_ERROR_CODE,
            datasetName, reason
        );
    }

    public CleaningException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### ExportException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class ExportException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "EXPORT_ERROR";

    public ExportException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ExportException(String format, String reason) {
        super(
            String.format("Error exporting to '%s': %s", format, reason),
            DEFAULT_ERROR_CODE,
            format, reason
        );
    }

    public ExportException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### RateLimitExceededException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class RateLimitExceededException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "RATE_LIMIT_EXCEEDED";

    public RateLimitExceededException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public RateLimitExceededException() {
        super("Too many requests. Please try again later", DEFAULT_ERROR_CODE);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

### DatabaseException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class DatabaseException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "DATABASE_ERROR";

    public DatabaseException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }

    public DatabaseException(String operation, String table) {
        super(
            String.format("Database error during '%s' on table '%s'", operation, table),
            DEFAULT_ERROR_CODE,
            operation, table
        );
    }
}
```

---

### ExternalServiceException.java

```java
package com.henri_fraise.hff_data_studio.exception;

public class ExternalServiceException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "EXTERNAL_SERVICE_ERROR";

    public ExternalServiceException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    public ExternalServiceException(String serviceName, String reason) {
        super(
            String.format("External service '%s' error: %s", serviceName, reason),
            DEFAULT_ERROR_CODE,
            serviceName, reason
        );
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }
}
```

---

## 3. ErrorCode enum

### ErrorCode.java

```java
package com.henri_fraise.hff_data_studio.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // Authentication & Authorization (1000-1099)
    UNAUTHORIZED("AUTH_1001", "Authentication required"),
    INVALID_CREDENTIALS("AUTH_1002", "Invalid credentials"),
    TOKEN_EXPIRED("AUTH_1003", "Token expired"),
    TOKEN_INVALID("AUTH_1004", "Invalid token"),
    FORBIDDEN("AUTH_1005", "Insufficient permissions"),
    ACCOUNT_DISABLED("AUTH_1006", "Account disabled"),

    // Resource (2000-2099)
    RESOURCE_NOT_FOUND("RES_2001", "Resource not found"),
    RESOURCE_ALREADY_EXISTS("RES_2002", "Resource already exists"),
    RESOURCE_ACCESS_DENIED("RES_2003", "Resource access denied"),

    // Validation (3000-3099)
    VALIDATION_ERROR("VAL_3001", "Validation error"),
    INVALID_EMAIL("VAL_3002", "Invalid email"),
    INVALID_PASSWORD("VAL_3003", "Invalid password"),
    INVALID_FILE_TYPE("VAL_3004", "Invalid file type"),
    INVALID_DATE_FORMAT("VAL_3005", "Invalid date format"),
    INVALID_ENUM_VALUE("VAL_3006", "Invalid enum value"),
    INVALID_PHONE_NUMBER("VAL_3007", "Invalid phone number"),
    INVALID_URL("VAL_3008", "Invalid URL"),
    INVALID_FILE_PATH("VAL_3009", "Invalid file path"),
    MAX_FILE_SIZE_EXCEEDED("VAL_3010", "Maximum file size exceeded"),

    // File Processing (4000-4099)
    FILE_UPLOAD_ERROR("FILE_4001", "File upload error"),
    FILE_DOWNLOAD_ERROR("FILE_4002", "File download error"),
    FILE_DELETE_ERROR("FILE_4003", "File delete error"),
    FILE_READ_ERROR("FILE_4004", "File read error"),
    FILE_WRITE_ERROR("FILE_4005", "File write error"),
    UNSUPPORTED_FILE_FORMAT("FILE_4006", "Unsupported file format"),
    FILE_CORRUPTED("FILE_4007", "File is corrupted"),
    FILE_EMPTY("FILE_4008", "File is empty"),

    // Data Processing (5000-5099)
    DATA_IMPORT_ERROR("DATA_5001", "Data import error"),
    DATA_EXPORT_ERROR("DATA_5002", "Data export error"),
    DATA_CLEANING_ERROR("DATA_5003", "Data cleaning error"),
    DATA_EXPLORATION_ERROR("DATA_5004", "Data exploration error"),
    DATA_NORMALIZATION_ERROR("DATA_5005", "Data normalization error"),
    DUPLICATE_DATA_FOUND("DATA_5006", "Duplicate data found"),
    INVALID_DATA_TYPE("DATA_5007", "Invalid data type"),
    MISSING_REQUIRED_DATA("DATA_5008", "Missing required data"),

    // Analysis (6000-6099)
    ANALYSIS_EXECUTION_ERROR("ANAL_6001", "Analysis execution error"),
    ANALYSIS_NOT_FOUND("ANAL_6002", "Analysis not found"),
    ANALYSIS_IN_PROGRESS("ANAL_6003", "Analysis in progress"),
    ANALYSIS_TIMEOUT("ANAL_6004", "Analysis timeout"),
    INVALID_ANALYSIS_PARAMETER("ANAL_6005", "Invalid analysis parameter"),

    // System (9000-9099)
    DATABASE_ERROR("SYS_9001", "Database error"),
    EXTERNAL_SERVICE_ERROR("SYS_9002", "External service error"),
    RATE_LIMIT_EXCEEDED("SYS_9003", "Rate limit exceeded"),
    INTERNAL_SERVER_ERROR("SYS_9004", "Internal server error"),
    SERVICE_UNAVAILABLE("SYS_9005", "Service unavailable"),

    // Audit (7000-7099)
    AUDIT_LOG_ERROR("AUD_7001", "Audit log error"),
    INSUFFICIENT_AUDIT_DATA("AUD_7002", "Insufficient audit data"),

    // Export (8000-8099)
    EXPORT_GENERATION_ERROR("EXP_8001", "Export generation error"),
    EXPORT_FORMAT_NOT_SUPPORTED("EXP_8002", "Export format not supported"),
    EXPORT_EMPTY_RESULTS("EXP_8003", "Export results are empty"),
    EXPORT_ZIP_CREATION_ERROR("EXP_8004", "ZIP creation error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
```

---

## 4. GlobalExceptionHandler

### GlobalExceptionHandler.java

```java
package com.henri_fraise.hff_data_studio.exception;

import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import com.henri_fraise.hff_data_studio.validation.handler.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== Business Exceptions ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            ResourceAlreadyExistsException ex,
            HttpServletRequest request) {
        
        log.warn("Resource already exists: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            UnauthorizedException ex,
            HttpServletRequest request) {
        
        log.warn("Unauthorized: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            ForbiddenException ex,
            HttpServletRequest request) {
        
        log.warn("Forbidden: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        
        log.warn("Invalid credentials: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            BusinessException ex,
            HttpServletRequest request) {
        
        log.warn("Token error: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            ValidationException ex,
            HttpServletRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .fieldErrors(ex.getFieldErrors() != null ? 
                ex.getFieldErrors().entrySet().stream()
                    .map(entry -> ValidationErrorResponse.FieldError.builder()
                        .field(entry.getKey())
                        .message(entry.getValue())
                        .build())
                    .collect(Collectors.toList()) : 
                null)
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
            BusinessException ex,
            HttpServletRequest request) {
        
        log.error("Processing error: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
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
            RateLimitExceededException ex,
            HttpServletRequest request) {
        
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            DatabaseException ex,
            HttpServletRequest request) {
        
        log.error("Database error: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
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
            ExternalServiceException ex,
            HttpServletRequest request) {
        
        log.error("External service error: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
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
            AuthenticationException ex,
            HttpServletRequest request) {
        
        log.warn("Authentication error: {}", ex.getMessage());
        
        String errorCode = ex instanceof BadCredentialsException ? 
            ErrorCode.INVALID_CREDENTIALS.getCode() : 
            ErrorCode.UNAUTHORIZED.getCode();
        
        ErrorResponse response = ErrorResponse.builder()
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
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));
        
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        log.warn("Constraint violation: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(fieldName, message);
        });
        
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        
        log.warn("Missing parameter: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        log.warn("Message not readable: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        
        log.error("File size exceeded: {}", ex.getMessage());
        
        long maxSize = ex.getMaxUploadSize();
        String sizeStr = maxSize > 0 ? 
            String.format("%.2f MB", maxSize / (1024.0 * 1024)) : 
            "unknown";
        
        ErrorResponse response = ErrorResponse.builder()
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
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
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
            DataAccessException ex,
            HttpServletRequest request) {
        
        log.error("Data access error: {}", ex.getMessage(), ex);
        
        ErrorResponse response = ErrorResponse.builder()
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
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            IllegalStateException ex,
            HttpServletRequest request) {
        
        log.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
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
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Unexpected error: ", ex);
        
        ErrorResponse response = ErrorResponse.builder()
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
```

---

## 5. Utilisation dans les Services

### UserService.java (extrait avec exceptions)

```java
package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.*;
import com.henri_fraise.hff_data_studio.mapper.UserMapper;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserCategoryService categoryService;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAll(pageable);
            return users.map(userMapper::toResponse);
        } catch (Exception ex) {
            log.error("Error retrieving users: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to retrieve users", ex);
        }
    }

    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return userMapper.toResponse(user);
    }

    public User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }

        try {
            UserCategory category = categoryService.getCategoryById(request.getCategoryId());
            User user = userMapper.toEntity(request, category);
            User saved = userRepository.save(user);
            log.info("User created successfully: {} ({})", saved.getEmail(), saved.getId());
            return userMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Failed to create user due to data integrity violation", ex);
        } catch (Exception ex) {
            log.error("Error creating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to create user", ex);
        }
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        User user = getUserEntityById(userId);

        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
            }
        }

        try {
            UserCategory category = null;
            if (request.getCategoryId() != null) {
                category = categoryService.getCategoryById(request.getCategoryId());
            }

            userMapper.updateEntity(user, request, category);
            User updated = userRepository.save(user);
            log.info("User updated successfully: {} ({})", updated.getEmail(), updated.getId());
            return userMapper.toResponse(updated);
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseException("Failed to update user due to data integrity violation", ex);
        } catch (Exception ex) {
            log.error("Error updating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to update user", ex);
        }
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserEntityById(userId);
        
        if (!user.getIsActive()) {
            throw new ValidationException("User is already deactivated");
        }
        
        try {
            user.setIsActive(false);
            userRepository.save(user);
            log.info("User deactivated successfully: {} ({})", user.getEmail(), userId);
        } catch (Exception ex) {
            log.error("Error deactivating user: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to deactivate user", ex);
        }
    }

    @Transactional
    public void updateLastLogin(UUID userId) {
        try {
            User user = getUserEntityById(userId);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception ex) {
            log.error("Error updating last login for user {}: {}", userId, ex.getMessage());
            // Non-critical error, log but don't throw
        }
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getUserEntityById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        // Validate new password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ValidationException("New password must be different from current password");
        }
        
        try {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Password changed successfully for user: {}", userId);
        } catch (Exception ex) {
            log.error("Error changing password: {}", ex.getMessage(), ex);
            throw new DatabaseException("Failed to change password", ex);
        }
    }
}
```

---

## Résumé des Exceptions

| Exception | Code | HTTP Status | Utilisation |
|-----------|------|-------------|-------------|
| ResourceNotFoundException | RESOURCE_NOT_FOUND | 404 | Entité non trouvée |
| ResourceAlreadyExistsException | RESOURCE_ALREADY_EXISTS | 409 | Duplicata |
| UnauthorizedException | UNAUTHORIZED | 401 | Non authentifié |
| ForbiddenException | FORBIDDEN | 403 | Pas de permission |
| InvalidCredentialsException | INVALID_CREDENTIALS | 401 | Mauvais identifiants |
| TokenExpiredException | TOKEN_EXPIRED | 401 | Token expiré |
| TokenInvalidException | TOKEN_INVALID | 401 | Token invalide |
| ValidationException | VALIDATION_ERROR | 400 | Erreur validation |
| FileProcessingException | FILE_PROCESSING_ERROR | 422 | Erreur fichier |
| AnalysisExecutionException | ANALYSIS_EXECUTION_ERROR | 422 | Erreur analyse |
| CleaningException | CLEANING_ERROR | 422 | Erreur nettoyage |
| ExportException | EXPORT_ERROR | 422 | Erreur export |
| RateLimitExceededException | RATE_LIMIT_EXCEEDED | 429 | Trop de requêtes |
| DatabaseException | DATABASE_ERROR | 500 | Erreur base |
| ExternalServiceException | EXTERNAL_SERVICE_ERROR | 503 | Erreur service externe |