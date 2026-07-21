package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.FilePathValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FilePathValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFilePath {

  String message() default "Invalid file path";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean allowNull() default false;

  boolean mustExist() default false;

  String[] allowedExtensions() default {};
}
