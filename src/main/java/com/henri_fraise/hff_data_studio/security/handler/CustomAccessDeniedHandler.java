package com.henri_fraise.hff_data_studio.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.henri_fraise.hff_data_studio.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(
          HttpServletRequest request,
          HttpServletResponse response,
          AccessDeniedException accessDeniedException)
          throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errorResponse =
            ErrorResponse.builder()
                    .code("FORBIDDEN")
                    .message("Vous n'avez pas la permission d'accéder à cette ressource")
                    .details(accessDeniedException.getMessage())
                    .timestamp(LocalDateTime.now())
                    .path(request.getRequestURI())
                    .method(request.getMethod())
                    .status(HttpStatus.FORBIDDEN.value())
                    .build();

    objectMapper.writeValue(response.getWriter(), errorResponse);
  }
}