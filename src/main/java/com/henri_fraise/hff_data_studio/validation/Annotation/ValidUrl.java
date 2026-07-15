package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.UrlValidator;
import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {

  String message() default
      "Invalid URL format. Expected format: https://www.example.com or http://www.example.com";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean allowNull() default false;

  boolean requireHttps() default false;

  String[] allowedProtocols() default {"http", "https"};
}
