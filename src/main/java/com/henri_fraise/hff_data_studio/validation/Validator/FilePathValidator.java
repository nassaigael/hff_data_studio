package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidFilePath;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Path;

public class FilePathValidator implements ConstraintValidator<ValidFilePath, String> {

  private boolean allowNull;
  private boolean mustExist;
  private String[] allowedExtensions;

  @Override
  public void initialize(ValidFilePath constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.mustExist = constraintAnnotation.mustExist();
    this.allowedExtensions = constraintAnnotation.allowedExtensions();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) return allowNull;

    String trimmed = value.trim();
    try {
      Path path = (Path) Paths.get(trimmed);

      if (mustExist && !Files.exists((java.nio.file.Path) path)) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("File does not exist: " + trimmed)
            .addConstraintViolation();
        return false;
      }

      boolean extensionAllowed =
          Arrays.stream(allowedExtensions)
              .anyMatch(ext -> trimmed.toLowerCase().endsWith("." + ext));
      if (!extensionAllowed) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "File extension not allowed. Allowed extensions: "
                    + String.join(", ", allowedExtensions))
            .addConstraintViolation();
        return false;
      }

      if (trimmed.contains("..")) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate("Path cannot contain '..'")
            .addConstraintViolation();
        return false;
      }
      return true;
    } catch (Exception e) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Invalid file path: " + trimmed)
          .addConstraintViolation();
      return false;
    }
  }
}
