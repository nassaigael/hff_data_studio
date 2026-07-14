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

	UUID excludeProjectId() default "0";
}

@Component
@RequiredArgsConstructor
class UniqueProjectNameValidator implements ConstraintValidator<UniqueProjectName, String> {

	private final ProjectRepository projectRepository;
	private UUID excludeProjectId;

	@Override
	public boolean isValid(String projectName, ConstraintValidatorContext context) {
		if (projectName == null || projectName.isEmpty()) return true;
		if (excludeProjectId != null && excludeProjectId != UUID.fromString("00000000-0000-0000-0000-000000000000"))
			return !projectRepository.existsByProjectNameAndIdNot(projectName, excludeProjectId);
		return !projectRepository.existsByProjectName(projectName);
	}
}
