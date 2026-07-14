package com.henri_fraise.hff_data_studio.exception;

public class TokenExpiredException extends BusinessException {

	private static final String DEFAULT_ERROR_CODE = "TOKEN_EXPIRED";

	public TokenExpiredException(String message) {
		super(message, DEFAULT_ERROR_CODE);
	}

	public TokenExpiredException() {
		super("Authentication token has expired. Please login again", DEFAULT_ERROR_CODE);
	}

	public TokenExpiredException(String message, Throwable cause) {
		super(message, DEFAULT_ERROR_CODE, cause);
	}
}