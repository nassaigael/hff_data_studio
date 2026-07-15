package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "dataset")
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Dataset {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "dataset_id")
  private UUID id;

  @Column(name = "dataset_name", nullable = false)
  private String datasetName;

  @Column(name = "row_count", nullable = false)
  @Builder.Default
  private Integer rowCount = 0;

  @Column(name = "column_count", nullable = false)
  @Builder.Default
  private Integer columnCount = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "is_cleaned", nullable = false)
  @Builder.Default
  private Boolean isCleaned = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id", nullable = false)
  private SourceFile sourceFile;

  @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL)
  private List<DatasetColumn> columns;

  @OneToOne(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
  private ExplorationReport explorationReport;

  @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL)
  private List<CleaningHistory> cleaningHistories;

  @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL)
  private List<AnalysisExecution> analysisExecutions;
}
