package com.henri_fraise.hff_data_studio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatisticsResponse {
  private Long totalProjects;
  private Long inProgress;
  private Long completed;
  private Long archived;
  private Long totalCreators;
  private Long newProjectsLast30Days;
}
