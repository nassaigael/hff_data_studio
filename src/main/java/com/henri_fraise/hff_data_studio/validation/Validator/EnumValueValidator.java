package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidEnumValue;
import java.util.Arrays;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValueValidator implements ConstraintValidator<ValidEnumValue, String> {

  private Class<? extends Enum<?>> enumClass;
  private boolean ignoreCase;
  private boolean allowNull;

  @Override
  public void initialize(ValidEnumValue constraintAnnotation) {
    this.enumClass = constraintAnnotation.enumClass();
    this.ignoreCase = constraintAnnotation.ignoreCase();
    this.allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) return allowNull;

    String trimmed = value.trim();

    for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
      String enumName = enumConstant.name();
      if (ignoreCase)
        if (enumName.equalsIgnoreCase(trimmed)) return true;
        else if (enumName.equals(trimmed)) return true;
    }
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(
            "" + "Value must be one of: " + Arrays.toString(enumClass.getEnumConstants()))
        .addConstraintViolation();
    return false;
  }
}
