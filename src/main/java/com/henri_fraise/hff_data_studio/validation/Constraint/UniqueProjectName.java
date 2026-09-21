package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import java.lang.annotation.*;
import java.util.UUID;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueProjectName {
  String message() default "Project name already exists";

  Class<?>[] groups() default {};

  Class<?>[] payload() default {};

  String excludeProjectId() default "";
}