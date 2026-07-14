package com.henri_fraise.hff_data_studio.exception;

public class TokenInvalidException extends BusinessException {

	private static final String DEFAULT_ERROR_CODE = "TOKEN_INVALID";

	public TokenInvalidException(String message) {
		super(message, DEFAULT_ERROR_CODE);
	}

	public TokenInvalidException() {
		super("Invalid authentication token", DEFAULT_ERROR_CODE);
	}

	public TokenInvalidException(String message, Throwable cause) {
		super(message, DEFAULT_ERROR_CODE, cause);
	}
}