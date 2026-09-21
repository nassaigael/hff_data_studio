package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

	private final UserRepository userRepository;

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		if (email == null || email.isEmpty()) return true;
		return !userRepository.existsByEmail(email);
	}
}