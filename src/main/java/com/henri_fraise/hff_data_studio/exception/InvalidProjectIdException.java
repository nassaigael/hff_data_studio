package com.henri_fraise.hff_data_studio.exception;

import lombok.Getter;

@Getter
public class InvalidProjectIdException extends RuntimeException {

	private final String value;

	public InvalidProjectIdException(String value) {
		super("Invalid projectId: " + value);
		this.value = value;
	}

}