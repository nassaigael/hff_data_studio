package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.*;
import java.util.UUID;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueProjectName {
	String message() default "Project name already exists";

	Class<?>[] groups() default {};

	Class<?>[] payload() default {};

	String excludeProjectId() default "";
}

@Component
@RequiredArgsConstructor
class UniqueProjectNameValidator implements ConstraintValidator<UniqueProjectName, String> {

	private final ProjectRepository projectRepository;
	private UUID excludeProjectId;

	@Override
	public void initialize(UniqueProjectName constraintAnnotation) {
		String value = constraintAnnotation.excludeProjectId();
		if (value == null || value.isBlank()) {
			this.excludeProjectId = null;
			return;
		}
		this.excludeProjectId = UUID.fromString(value);
	}

	@Override
	public boolean isValid(String projectName, ConstraintValidatorContext context) {
		if (projectName == null || projectName.isEmpty()) return true;
		if (excludeProjectId != null && excludeProjectId != UUID.fromString("00000000-0000-0000-0000-000000000000"))
			return !projectRepository.existsByProjectNameAndIdNot(projectName, excludeProjectId);
		return !projectRepository.existsByProjectName(projectName);
	}
}
