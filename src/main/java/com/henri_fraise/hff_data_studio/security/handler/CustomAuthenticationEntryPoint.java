package com.henri_fraise.hff_data_studio.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code("UNAUTHORIZED")
            .message("Authentication required to access this resource")
            .details(authException.getMessage())
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .method(request.getMethod())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}
