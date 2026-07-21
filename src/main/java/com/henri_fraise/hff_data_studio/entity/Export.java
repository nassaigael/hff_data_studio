package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "export")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Export {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "export_id")
  private UUID id;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "export_format", nullable = false)
  private ExportFormat exportFormat;

  @CreationTimestamp
  @Column(name = "exported_at", nullable = false, updatable = false)
  private LocalDateTime exportedAt;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "execution_id", nullable = false)
  private AnalysisExecution execution;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
