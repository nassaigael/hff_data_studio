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
