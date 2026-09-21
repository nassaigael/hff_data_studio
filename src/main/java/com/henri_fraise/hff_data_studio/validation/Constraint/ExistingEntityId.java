package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.enums.EntityType;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExistingEntityId {
  String message() default "Entity does not exist";

  Class<?>[] groups() default {};

  Class<?>[] payload() default {};

  EntityType entityType();
}
