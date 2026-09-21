package com.henri_fraise.hff_data_studio.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreationRequest {

  @NotBlank(message = "Category label is required")
  private String label;

  private String description;

  @NotNull(message = "Access level is required")
  private Integer accessLevel;
}
