package com.henri_fraise.hff_data_studio.validation.Validator;

import com.henri_fraise.hff_data_studio.validation.Annotation.ValidPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

	private int maxLength;
	private int minLength;
	private boolean requireUpperCase;
	private boolean requireLowerCase;
	private boolean requireDigit;
	private boolean requireSpecialChar;

	@Override
	public void initialize(ValidPassword constraintAnnotation) {
		this.maxLength = constraintAnnotation.maxLength();
		this.minLength = constraintAnnotation.minLength();
		this.requireUpperCase = constraintAnnotation.requireUppercase();
		this.requireLowerCase = constraintAnnotation.requireLowercase();
		this.requireDigit = constraintAnnotation.requireDigit();
		this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		if (value == null || value.trim().isEmpty()) return false;

		List<String> errors = new ArrayList<>();

		if (value.length() < minLength) errors.add("Password must be at least " + minLength + " characters");

		if (value.length() > maxLength) errors.add("Password must not exceed " + maxLength + " characters");

		if (requireUpperCase && !value.matches(".*[A-Z].*"))
			errors.add("Password must contain at least one uppercase letter");

		if (requireLowerCase && !value.matches(".*[a-z].*"))
			errors.add("Password must contain at least one lowercase letter");

		if (requireDigit && !value.matches(".*\\d.*")) errors.add("Password must contain at least one digit");

		if (requireSpecialChar && !value.matches(".*[!@#$%^&*()_+=-\\\\\\\\{};':\"|,.<>/?].*"))
			errors.add("Password must contain at least one special character");

		if (errors.isEmpty()) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(String.join("; ", errors)).addConstraintViolation();
			return false;
		}
		return true;
	}
}
