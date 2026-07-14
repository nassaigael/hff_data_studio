package com.henri_fraise.hff_data_studio.exception;

public class AnalysisExecutionException extends BusinessException {

	private static final String DEFAULT_ERROR_CODE = "ANALYSIS_EXECUTION_ERROR";

	public AnalysisExecutionException(String message) {
		super(message, DEFAULT_ERROR_CODE);
	}

	public AnalysisExecutionException(String analysisName, String reason) {
		super(
				String.format("Error executing analysis '%s': %s", analysisName, reason),
				DEFAULT_ERROR_CODE,
				analysisName, reason
		);
	}

	public AnalysisExecutionException(String message, Throwable cause) {
		super(message, DEFAULT_ERROR_CODE, cause);
	}
}