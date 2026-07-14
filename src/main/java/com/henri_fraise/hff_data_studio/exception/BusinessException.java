package com.henri_fraise.hff_data_studio.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

	private final String errorCode;
	private final Object[] args;

	protected BusinessException(String message) {
		super(message);
		this.errorCode = "BUSINESS_ERROR";
		this.args = null;
	}

	protected BusinessException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
		this.args = null;
	}

	protected BusinessException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = "BUSINESS_ERROR";
		this.args = null;
	}

	protected BusinessException(String message, String errorCode, Object... args) {
		super(message);
		this.errorCode = errorCode;
		this.args = args;
	}

	protected BusinessException(String message, String errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		this.args = null;
	}
}