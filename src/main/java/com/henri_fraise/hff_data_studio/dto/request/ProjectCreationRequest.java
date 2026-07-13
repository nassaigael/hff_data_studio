package com.henri_fraise.hff_data_studio.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreationRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String description;
}
