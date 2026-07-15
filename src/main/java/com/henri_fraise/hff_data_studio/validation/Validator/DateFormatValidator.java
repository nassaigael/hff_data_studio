package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {

  private String pattern;
  private boolean allowNull;

  @Override
  public void initialize(ValidDateFormat constraintAnnotation) {
    this.pattern = constraintAnnotation.pattern();
    this.allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) return allowNull;

    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
      if (pattern.contains("HH") || pattern.contains("mm") || pattern.contains("ss"))
        LocalDateTime.parse(value.trim(), formatter);
      else LocalDate.parse(value.trim(), formatter);
      return true;
    } catch (DateTimeParseException e) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Date must be in format: " + pattern)
          .addConstraintViolation();
      return false;
    }
  }
}
