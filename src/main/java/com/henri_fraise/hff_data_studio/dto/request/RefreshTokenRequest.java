package com.henri_fraise.hff_data_studio.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

  @NotBlank(message = "Refresh token is required")
  private String refreshToken;
}
