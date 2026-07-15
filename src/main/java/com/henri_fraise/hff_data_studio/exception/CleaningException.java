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
        datasetName,
        reason);
  }

  public CleaningException(String message, Throwable cause) {
    super(message, DEFAULT_ERROR_CODE, cause);
  }
}
