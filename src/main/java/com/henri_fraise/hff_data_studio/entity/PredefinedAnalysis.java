package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "predefined_analysis")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PredefinedAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "analysis_id")
    private UUID id;

    @Column(name = "analysis_name", nullable = false)
    private String analysisName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private AnalysisCategory category;

    @Column(name = "reference_script", nullable = false)
    private String referenceScript;

    @Column(name = "required_parameters_json", columnDefinition = "TEXT")
    private String requiredParametersJson;

    @OneToMany(mappedBy = "predefinedAnalysis")
    private List<AnalysisExecution> executions;
}
