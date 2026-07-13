package com.henri_fraise.hff_data_studio.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreationRequest {

    @NotBlank(message = "Category label is required")
    private String label;

    private String description;

    @NotNull(message = "Acces level is required")
    private Integer accessLevel;
}
