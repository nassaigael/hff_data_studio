package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.AnalysisExecutionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@Entity
@Table(name = "analysis_execution")
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "execution_id")
    private UUID id;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AnalysisExecutionStatus status = AnalysisExecutionStatus.IN_PROGRESS;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "used_parameters_json", columnDefinition = "TEXT")
    private String usedParametersJson;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id")
    private PredefinedAnalysis predefinedAnalysis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "execution")
    private List<AnalysisResult> results;

    @OneToMany(mappedBy = "execution")
    private List<Export> exports;
}