package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.enums.EntityType;
import com.henri_fraise.hff_data_studio.repository.ProjectRepository;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.validation.Constraint.ExistingEntityId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.UUID;

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