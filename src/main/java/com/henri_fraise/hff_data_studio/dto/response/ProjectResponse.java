package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private UUID projectId;
    private String projectName;
    private String description;
    private LocalDateTime createdAt;
    private ProjectStatus status;
    private UUID creatorId;
    private String creatorFullName;
    private Long fileCount;
    private Long datasetCount;
}
