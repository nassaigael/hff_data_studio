package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidEmail;
import java.util.Arrays;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private boolean allowNull;
  private boolean allowEmpty;
  private String[] allowedDomains;

  @Override
  public void initialize(ValidEmail constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
    this.allowEmpty = constraintAnnotation.allowEmpty();
    this.allowedDomains = constraintAnnotation.allowedDomains();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {

    if (value == null) return allowNull;

    if (value.trim().isEmpty()) return allowEmpty;

    if (!EMAIL_PATTERN.matcher(value).matches()) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate("Email format is invalid")
          .addConstraintViolation();
      return false;
    }

    if (allowedDomains.length > 0) {
      String domain = value.substring(value.indexOf("@") + 1);
      boolean domainAllowed =
          Arrays.stream(allowedDomains)
              .anyMatch(
                  allowed -> domain.equalsIgnoreCase(allowed) || domain.endsWith("." + allowed));
      if (!domainAllowed) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "Email domain must be one of: " + String.join(", ", allowedDomains))
            .addConstraintViolation();
        return false;
      }
    }
    return true;
  }
}
