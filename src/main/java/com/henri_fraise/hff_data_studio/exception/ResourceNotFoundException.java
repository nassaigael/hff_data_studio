package com.henri_fraise.hff_data_studio.exception;

public class ResourceNotFoundException extends BusinessException {

  private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

  public ResourceNotFoundException(String message) {
    super(message, DEFAULT_ERROR_CODE);
  }

  public ResourceNotFoundException(String entityName, Object id) {
    super(
        String.format("%s not found with id: %s", entityName, id),
        DEFAULT_ERROR_CODE,
        entityName,
        id);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, DEFAULT_ERROR_CODE, cause);
  }

  public ResourceNotFoundException(String entityName, String fieldName, Object fieldValue) {
    super(
        String.format("%s not found with %s: %s", entityName, fieldName, fieldValue),
        DEFAULT_ERROR_CODE,
        entityName,
        fieldName,
        fieldValue);
  }
}
