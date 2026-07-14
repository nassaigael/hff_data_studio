package com.henri_fraise.hff_data_studio.exception;

public class TokenExpiredException extends RuntimeException {
	public TokenExpiredException(String message) {
		super(message);
	}
}
