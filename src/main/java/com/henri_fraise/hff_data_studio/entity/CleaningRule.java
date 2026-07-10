package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.RuleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cleaning_rule")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CleaningRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rule_id")
    private UUID id;

    @Column(name = "rule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    @Column(name = "parameters_json", columnDefinition = "TEXT")
    private String parametersJson;

    @Column(name = "execution_order", nullable = false)
    @Builder.Default
    private Integer executionOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    private DatasetColumn column;

    @OneToMany(mappedBy = "rule")
    private List<CleaningHistory> cleaningHistories;
}