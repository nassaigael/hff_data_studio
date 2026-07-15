package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidPhoneNumber;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

  private static final Pattern PHONE_PATTERN =
      Pattern.compile(
          "^[+]?[0-9]{1,4}?[-.\\s]?[(]?[0-9]{1,3}[)]?[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,4}[-.\\s]?[0-9]{1,9}$");

  private boolean allowNull;
  private boolean allowEmpty;
  private String countryCode;

  @Override
  public void initialize(ValidPhoneNumber constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.allowEmpty = constraintAnnotation.allowEmpty();
    this.countryCode = constraintAnnotation.countryCode();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return allowNull;
    if (value.trim().isEmpty()) return allowEmpty;

    String cleaned = value.trim();
    if (!countryCode.isEmpty() && !cleaned.startsWith(countryCode)) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Phone number must start with country code: " + countryCode)
          .addConstraintViolation();
      return false;
    }

    if (!PHONE_PATTERN.matcher(cleaned).matches()) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              "Phone number format is invalid. Excepted format: [+][country code][number]")
          .addConstraintViolation();
      return false;
    }
    return true;
  }
}
