package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.PhoneNumberValidator;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

  String message() default "Invalid phone number format. Expected format: +261389682194";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean allowNull() default false;

  boolean allowEmpty() default false;

  String countryCode() default "+261";
}
