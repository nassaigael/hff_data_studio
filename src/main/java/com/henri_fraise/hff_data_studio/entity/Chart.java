package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ChartType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "chart")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "chart_id")
    private UUID id;

    @Column(name = "chart_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChartType chartType;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @OneToOne
    @JoinColumn(name = "result_id", nullable = false, unique = true)
    private AnalysisResult result;
}