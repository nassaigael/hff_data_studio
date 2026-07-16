package com.henri_fraise.hff_data_studio.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(
			@NonNull
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {
		log.info("Authentication successful: {}", authentication.getName());
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write("Authentication successful");
	}
}