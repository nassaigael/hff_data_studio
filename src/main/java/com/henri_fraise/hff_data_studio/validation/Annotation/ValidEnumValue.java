package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.EnumValueValidator;
import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnumValue {

  String message() default "Invalid enum value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  Class<? extends Enum<?>> enumClass();

  boolean ignoreCase() default false;

  boolean allowNull() default false;
}
