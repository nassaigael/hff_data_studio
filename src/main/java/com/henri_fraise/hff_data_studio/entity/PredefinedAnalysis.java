package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import jakarta.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "predefined_analysis")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredefinedAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "analysis_id")
  private UUID id;

  @Column(name = "analysis_name", length = 150, nullable = false)
  private String analysisName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "category", length = 60, nullable = false)
  private AnalysisCategory category;

  @Column(name = "reference_script", length = 255, nullable = false)
  private String referenceScript;

  @Column(name = "required_parameters_json", columnDefinition = "JSONB")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> requiredParametersJson;

  @OneToMany(mappedBy = "predefinedAnalysis")
  private List<AnalysisExecution> analysisExecutions;
}
