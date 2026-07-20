package com.henri_fraise.hff_data_studio.security.config;

import com.henri_fraise.hff_data_studio.security.filter.JwtAuthenticationFilter;
import com.henri_fraise.hff_data_studio.security.handler.CustomAccessDeniedHandler;
import com.henri_fraise.hff_data_studio.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	private static final String[] PUBLIC_ENDPOINTS = {
			"/api/v1/auth/**",
			"/api/v1/health",
			"/api/v1/actuator/health",
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/v3/api-docs/**",
			"/v3/api-docs.yaml",
			"/api/v1/actuator/info"
	};

	private static final String[] ADMIN_ENDPOINTS = {
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

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
						.requestMatchers(ADMIN_ENDPOINTS).hasAuthority("ADMIN")
						.requestMatchers(USER_ENDPOINTS).authenticated()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
		authBuilder
				.userDetailsService(userDetailsService)
				.passwordEncoder(passwordEncoder());
		return authBuilder.build();
	}

	@Bean
	@Qualifier("authenticationManagerBean")
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
				"http://localhost:3000",
				"http://localhost:3001",
				"http://localhost:8080",
				"https://catalyst.hff.re"
		));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList(
				"Authorization",
				"Content-Type",
				"X-Requested-With",
				"Accept",
				"Origin",
				"Access-Control-Request-Method",
				"Access-Control-Request-Headers"
		));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}