package com.henri_fraise.hff_data_studio.exception;

public class RateLimitExceededException extends RuntimeException {
	public RateLimitExceededException(String message) {
		super(message);
	}
}
