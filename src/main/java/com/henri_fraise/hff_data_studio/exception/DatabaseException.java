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
        operation,
        table);
  }
}
