package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;

  private String refreshToken;

  private Long expiresIn;

  @Builder.Default private String tokerType = "Bearer";
}
