package com.henri_fraise.hff_data_studio.exception;

public class ForbiddenException extends BusinessException {

	private static final String DEFAULT_ERROR_CODE = "FORBIDDEN";

	public ForbiddenException(String message) {
		super(message, DEFAULT_ERROR_CODE);
	}

	public ForbiddenException() {
		super("You don't have permission to access this resource", DEFAULT_ERROR_CODE);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(message, DEFAULT_ERROR_CODE, cause);
	}
}