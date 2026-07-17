package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.enums.ResultType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "analysis_result")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "result_id")
  private UUID id;

  @Column(name = "result_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private ResultType resultType;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Column(name = "file_format", nullable = false)
  @Enumerated(EnumType.STRING)
  private FileType fileFormat;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "execution_id", nullable = false)
  private AnalysisExecution execution;

  @OneToOne(mappedBy = "result", cascade = CascadeType.ALL)
  private Chart chart;
}
