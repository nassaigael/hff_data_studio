package com.henri_fraise.hff_data_studio.validation.Constraint;

import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueEmail {

	String message() default "Email already exists";

	Class<?>[] groups() default {};

	Class<?>[] payload() default {};
}

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
