package com.henri_fraise.hff_data_studio.exception;

public class UserDisabledException extends BusinessException {

  private static final String DEFAULT_ERROR_CODE = "USER_DISABLED";

  public UserDisabledException(String message) {
    super(message, DEFAULT_ERROR_CODE);
  }

  public UserDisabledException() {
    super("User account is disabled", DEFAULT_ERROR_CODE);
  }
}
