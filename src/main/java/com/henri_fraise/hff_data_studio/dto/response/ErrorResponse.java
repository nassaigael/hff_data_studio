package com.henri_fraise.hff_data_studio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private String code;
  private String message;
  private String details;
  private LocalDateTime timestamp;
  private String path;
  private String method;
  private Integer status;
  private List<String> errors;
  private Map<String, String> validationErrors;
}