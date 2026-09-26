package com.henri_fraise.hff_data_studio.tenant;

public final class TenantContext {

	private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

	private TenantContext() {}

	public static void setTenant(String tenantCode) {
		CURRENT_TENANT.set(tenantCode);
	}

	public static String getTenant() {
		return CURRENT_TENANT.get();
	}

	public static void clear() {
		CURRENT_TENANT.remove();
	}
}