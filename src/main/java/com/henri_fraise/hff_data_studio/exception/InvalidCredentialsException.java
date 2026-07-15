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
