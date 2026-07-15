package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidFileType;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileTypeValidator implements ConstraintValidator<ValidFileType, MultipartFile> {

  private String[] allowedTypes;
  private long maxSize;

  @Override
  public void initialize(ValidFileType constraintAnnotation) {
    this.allowedTypes = constraintAnnotation.allowedTypes();
    this.maxSize = constraintAnnotation.maxSize();
  }

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {

    if (file == null) return false;

    if (file.getSize() > maxSize) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "File size exceeds maximum allowed size " + (maxSize / (1024 * 1024)) + " MB")
          .addConstraintViolation();
      return false;
    }

    String fileName = file.getOriginalFilename();
    if (fileName == null) return false;

    String extension;
    int lastDot = fileName.lastIndexOf(".");
    if (lastDot > 0) extension = fileName.substring(lastDot + 1).toUpperCase();
    else {
      extension = "";
    }

    String contentType = file.getContentType();

    boolean typeAllowed =
        Arrays.stream(allowedTypes)
            .anyMatch(
                type -> {
                  return switch (type) {
                    case "CSV" ->
                        extension.equals("CSV")
                            || (contentType != null && contentType.contains("csv"));
                    case "EXCEL" ->
                        extension.equals("XLSX")
                            || extension.equals("XLS")
                            || (contentType != null
                                && (contentType.contains("excel")
                                    || contentType.contains("spreadsheet")));
                    case "SQL" ->
                        extension.equals("SQL")
                            || (contentType != null && contentType.contains("sql"));
                    default -> false;
                  };
                });

    if (!typeAllowed) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "File type not allowed. Allowed types: " + String.join(", ", allowedTypes))
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
