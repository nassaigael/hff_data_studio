package com.henri_fraise.hff_data_studio.exception;

public class FileProcessingException extends BusinessException {

	private static final String DEFAULT_ERROR_CODE = "FILE_PROCESSING_ERROR";

	public FileProcessingException(String message) {
		super(message, DEFAULT_ERROR_CODE);
	}

	public FileProcessingException(String fileName, String reason) {
		super(
				String.format("Error processing file '%s': %s", fileName, reason),
				DEFAULT_ERROR_CODE,
				fileName, reason
		);
	}

	public FileProcessingException(String message, Throwable cause) {
		super(message, DEFAULT_ERROR_CODE, cause);
	}
}