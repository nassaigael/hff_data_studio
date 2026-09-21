package com.henri_fraise.hff_data_studio.validation.Constraint;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueProjectName {
  String message() default "Project name already exists";

  Class<?>[] groups() default {};

  Class<?>[] payload() default {};

  String excludeProjectId() default "";
}
