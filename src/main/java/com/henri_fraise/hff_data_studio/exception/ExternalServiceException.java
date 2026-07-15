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
        serviceName,
        reason);
  }

  public ExternalServiceException(String message, Throwable cause) {
    super(message, DEFAULT_ERROR_CODE, cause);
  }
}
