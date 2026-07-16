package com.henri_fraise.hff_data_studio.security.config;

import com.henri_fraise.hff_data_studio.security.SecurityExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityExpressionConfig {

	@Bean
	public SecurityExpression securityExpression() {
		return new SecurityExpression();
	}
}