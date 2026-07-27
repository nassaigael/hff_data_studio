package com.henri_fraise.hff_data_studio.enums;

public enum UserRole {

	ADMIN("Administrator", "Full access to all features"),
	DATA_ANALYST("Data Analyst", "Full access to data analysis features"),
	CONSULTANT("Consultant", "Read and export only"),
	INVITE("Guest", "Restricted read access");

	private final String displayName;
	private final String description;

	UserRole(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public static UserRole fromLabel(String label) {
		for (UserRole role : UserRole.values()) {
			if (role.name().equalsIgnoreCase(label)) {
				return role;
			}
		}
		throw new IllegalArgumentException("Unknown role: " + label);
	}

	public boolean isAdmin() {
		return this == ADMIN;
	}

	public boolean isDataAnalyst() {
		return this == DATA_ANALYST;
	}

	public boolean isConsultant() {
		return this == CONSULTANT;
	}

	public boolean isInvite() {
		return this == INVITE;
	}

	public boolean hasAccessLevel(int requiredLevel) {
		return this.getAccessLevel() >= requiredLevel;
	}

	public int getAccessLevel() {
		return switch (this) {
			case ADMIN -> 5;
			case DATA_ANALYST -> 4;
			case CONSULTANT -> 2;
			case INVITE -> 1;
		};
	}
}