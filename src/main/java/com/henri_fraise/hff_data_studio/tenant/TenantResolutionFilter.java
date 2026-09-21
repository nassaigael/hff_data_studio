package com.henri_fraise.hff_data_studio.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
@Slf4j
public class TenantResolutionFilter extends OncePerRequestFilter {

	private static final String TENANT_HEADER = "X-Tenant-Id";

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String tenant = request.getHeader(TENANT_HEADER);
			if (tenant == null || tenant.isBlank()) {
				String host = request.getServerName();
				tenant = extractTenantFromHost(host);
			}
			if (tenant != null && !tenant.isBlank()) {
				TenantContext.setTenant(tenant);
			}
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear();
		}
	}

	private String extractTenantFromHost(String host) {
		if (host == null) return null;
		String[] parts = host.split("\\.");
		return parts.length > 2 ? parts[0] : null;
	}
}