package com.henri_fraise.hff_data_studio.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

	UNAUTHORIZED("AUTH_1001", "Authentication required"),
	INVALID_CREDENTIALS("AUTH_1002", "Invalid credentials"),
	TOKEN_EXPIRED("AUTH_1003", "Token expired"),
	TOKEN_INVALID("AUTH_1004", "Invalid token"),
	FORBIDDEN("AUTH_1005", "Insufficient permissions"),
	ACCOUNT_DISABLED("AUTH_1006", "Account disabled"),

	RESOURCE_NOT_FOUND("RES_2001", "Resource not found"),
	RESOURCE_ALREADY_EXISTS("RES_2002", "Resource already exists"),
	RESOURCE_ACCESS_DENIED("RES_2003", "Resource access denied"),

	VALIDATION_ERROR("VAL_3001", "Validation error"),
	INVALID_EMAIL("VAL_3002", "Invalid email"),
	INVALID_PASSWORD("VAL_3003", "Invalid password"),
	INVALID_FILE_TYPE("VAL_3004", "Invalid file type"),
	INVALID_DATE_FORMAT("VAL_3005", "Invalid date format"),
	INVALID_ENUM_VALUE("VAL_3006", "Invalid enum value"),
	INVALID_PHONE_NUMBER("VAL_3007", "Invalid phone number"),
	INVALID_URL("VAL_3008", "Invalid URL"),
	INVALID_FILE_PATH("VAL_3009", "Invalid file path"),
	MAX_FILE_SIZE_EXCEEDED("VAL_3010", "Maximum file size exceeded"),

	FILE_UPLOAD_ERROR("FILE_4001", "File upload error"),
	FILE_DOWNLOAD_ERROR("FILE_4002", "File download error"),
	FILE_DELETE_ERROR("FILE_4003", "File delete error"),
	FILE_READ_ERROR("FILE_4004", "File read error"),
	FILE_WRITE_ERROR("FILE_4005", "File write error"),
	UNSUPPORTED_FILE_FORMAT("FILE_4006", "Unsupported file format"),
	FILE_CORRUPTED("FILE_4007", "File is corrupted"),
	FILE_EMPTY("FILE_4008", "File is empty"),

	DATA_IMPORT_ERROR("DATA_5001", "Data import error"),
	DATA_EXPORT_ERROR("DATA_5002", "Data export error"),
	DATA_CLEANING_ERROR("DATA_5003", "Data cleaning error"),
	DATA_EXPLORATION_ERROR("DATA_5004", "Data exploration error"),
	DATA_NORMALIZATION_ERROR("DATA_5005", "Data normalization error"),
	DUPLICATE_DATA_FOUND("DATA_5006", "Duplicate data found"),
	INVALID_DATA_TYPE("DATA_5007", "Invalid data type"),
	MISSING_REQUIRED_DATA("DATA_5008", "Missing required data"),

	ANALYSIS_EXECUTION_ERROR("ANAL_6001", "Analysis execution error"),
	ANALYSIS_NOT_FOUND("ANAL_6002", "Analysis not found"),
	ANALYSIS_IN_PROGRESS("ANAL_6003", "Analysis in progress"),
	ANALYSIS_TIMEOUT("ANAL_6004", "Analysis timeout"),
	INVALID_ANALYSIS_PARAMETER("ANAL_6005", "Invalid analysis parameter"),

	DATABASE_ERROR("SYS_9001", "Database error"),
	EXTERNAL_SERVICE_ERROR("SYS_9002", "External service error"),
	RATE_LIMIT_EXCEEDED("SYS_9003", "Rate limit exceeded"),
	INTERNAL_SERVER_ERROR("SYS_9004", "Internal server error"),
	SERVICE_UNAVAILABLE("SYS_9005", "Service unavailable"),

	AUDIT_LOG_ERROR("AUD_7001", "Audit log error"),
	INSUFFICIENT_AUDIT_DATA("AUD_7002", "Insufficient audit data"),

	EXPORT_GENERATION_ERROR("EXP_8001", "Export generation error"),
	EXPORT_FORMAT_NOT_SUPPORTED("EXP_8002", "Export format not supported"),
	EXPORT_EMPTY_RESULTS("EXP_8003", "Export results are empty"),
	EXPORT_ZIP_CREATION_ERROR("EXP_8004", "ZIP creation error");

	private final String code;
	private final String defaultMessage;

	ErrorCode(String code, String defaultMessage) {
		this.code = code;
		this.defaultMessage = defaultMessage;
	}
}