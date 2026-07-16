package com.henri_fraise.hff_data_studio.security.config;

import com.henri_fraise.hff_data_studio.security.filter.JwtAuthenticationFilter;
import com.henri_fraise.hff_data_studio.security.handler.CustomAccessDeniedHandler;
import com.henri_fraise.hff_data_studio.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	private static final String[] PUBLICS_ENDPOINTS = {
			"/api/v1/auth/**",
			"/api/v1/health",
			"/api/v1/actuator/health",
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/v3/api-docs/**",
			"/v3/api-docs.yaml",
			"/api/v1/actuator/info"
	};

	private static  final String[] ADMIN_ENDPOINTS = {
			"/api/v1/admin/**",
			"/api/v1/users/**",
			"/api/v1/categories/**",
			"/api/v1/permissions/**",
			"/api/v1/audit/**"
	};

	private static final String[] USER_ENDPOINTS = {
			"/api/v1/projects/**",
			"/api/v1/files/**",
			"/api/v1/datasets/**",
			"/api/v1/exploration/**",
			"/api/v1/cleaning/**",
			"/api/v1/analyses/**",
			"/api/v1/executions/**",
			"/api/v1/exports/**",
			"/api/v1/results/**"
	};

}
