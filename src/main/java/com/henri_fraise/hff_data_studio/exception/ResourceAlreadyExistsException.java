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
        entityName,
        fieldName,
        fieldValue);
  }

  public ResourceAlreadyExistsException(String message, Throwable cause) {
    super(message, DEFAULT_ERROR_CODE, cause);
  }
}
