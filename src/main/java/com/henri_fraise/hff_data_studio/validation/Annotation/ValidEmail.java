package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.EmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

	String DEFAULT_MESSAGE = "Email must be valid and from an allowed domain";

	String message() default DEFAULT_MESSAGE;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean allowNull() default false;

	boolean allowEmpty() default false;

	String[] allowedDomains() default {};
}
