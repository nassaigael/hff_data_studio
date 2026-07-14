package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.enums.EntityType;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.*;
import java.util.UUID;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExistingEntityId {
	String message() default "Entity does not exist";

	Class<?>[] groups() default {};

	Class<?>[] payload() default {};

	EntityType entityType();
}

@Component
@RequiredArgsConstructor
class ExistingEntityIdValidator implements ConstraintValidator<ExistingEntityId, UUID> {

	private final UserRepository userRepository;
	private final ProjectRepository projectRepository;
	private EntityType entityType;

	@Override
	public void initialize(ExistingEntityId constraintAnnotation) {
		this.entityType = constraintAnnotation.entityType();
	}

	@Override
	public boolean isValid(UUID id, ConstraintValidatorContext context) {
		if (id == null) return true;

		return switch (entityType) {
			case USER -> userRepository.existsById(id);
			case PROJECT -> projectRepository.existsById(id);
			case ANALYSIS, FILE, DATASET -> true;
		};
	}
}
