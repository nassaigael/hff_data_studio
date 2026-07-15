package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.PasswordValidator;
import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

  String message() default
      "Password must be at least 8 characters, contain uppercase, lowercase, digit and character";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  int minLength() default 8;

  int maxLength() default 32;

  boolean requireUppercase() default true;

  boolean requireLowercase() default true;

  boolean requireDigit() default true;

  boolean requireSpecialChar() default true;
}
