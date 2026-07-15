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
        format,
        reason);
  }

  public ExportException(String message, Throwable cause) {
    super(message, DEFAULT_ERROR_CODE, cause);
  }
}
