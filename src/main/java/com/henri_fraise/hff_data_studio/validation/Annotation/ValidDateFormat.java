package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.DateFormatValidator;
import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DateFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {

  String message() default "Invalid date format. Expected format: yyyy-MM-dd";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String pattern() default "yyyy-MM-dd";

  boolean allowNull() default false;
}
