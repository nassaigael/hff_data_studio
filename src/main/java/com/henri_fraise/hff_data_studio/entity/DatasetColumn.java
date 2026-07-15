package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ColumnType;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatasetColumn {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "column_id")
  private UUID id;

  @Column(name = "original_name", nullable = false)
  private String originalName;

  @Column(name = "normalized_name", nullable = false)
  private String normalizedName;

  @Column(name = "detected_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private ColumnType detectedType;

  @Column(name = "target_type", nullable = false)
  private ColumnType targetType;

  @Column(name = "position", nullable = false)
  private Integer position;

  @Column(name = "null_count", nullable = false)
  @Builder.Default
  private Integer nullCount = 0;

  @Column(name = "unique_count", nullable = false)
  @Builder.Default
  private Integer uniqueCount = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dataset_id", nullable = false)
  private Dataset dataset;

  @OneToMany(mappedBy = "column")
  private List<CleaningRule> cleaningRules;
}
