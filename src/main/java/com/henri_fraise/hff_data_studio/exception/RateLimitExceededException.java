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
