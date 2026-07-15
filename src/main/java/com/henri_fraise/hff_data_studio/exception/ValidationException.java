package com.henri_fraise.hff_data_studio.exception;

import java.util.List;
import java.util.Map;
import lombok.Getter;

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

  public ValidationException(
      String message, Map<String, String> fieldErrors, List<String> globalErrors) {
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
