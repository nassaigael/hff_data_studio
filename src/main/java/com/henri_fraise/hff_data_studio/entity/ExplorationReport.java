package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "exploration_report")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExplorationReport {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "report_id")
  private UUID id;

  @CreationTimestamp
  @Column(name = "generated_at", nullable = false, updatable = false)
  private LocalDateTime generatedAt;

  @Column(name = "total_rows", nullable = false)
  private Integer totalRows;

  @Column(name = "duplicate_count", nullable = false)
  @Builder.Default
  private Integer duplicateCount = 0;

  @Column(name = "missing_values_count", nullable = false)
  @Builder.Default
  private Integer missingValuesCount = 0;

  @Column(name = "quality_score", nullable = false, precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal qualityScore = BigDecimal.ZERO;

  @Column(name = "report_pdf_path")
  private String reportPdfPath;

  @OneToOne
  @JoinColumn(name = "dataset_id", nullable = false, unique = true)
  private Dataset dataset;
}
