package com.henri_fraise.hff_data_studio.validation.Annotation;

import com.henri_fraise.hff_data_studio.validation.Validator.FileTypeValidator;
import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = FileTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFileType {

  String message() default "File type not allowed. Allowed types are: CSV, EXCEL, SQL";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] allowedTypes() default {"CSV", "EXCEL", "SQL"};

  long maxSize() default 200 * 1024 * 1024;
}
