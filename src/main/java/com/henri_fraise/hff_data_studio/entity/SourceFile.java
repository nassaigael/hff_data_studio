package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.FileFormat;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Builder
@Getter
@Setter
@Entity
@Table(name = "source_file")
@AllArgsConstructor
@NoArgsConstructor
public class SourceFile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "file_id")
  private UUID id;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private FileFormat fileFormat;

  @Column(name = "storage_path", nullable = false)
  private String storagePath;

  @Column(name = "size_bytes", nullable = false)
  private Long sizeBytes;

  @CreationTimestamp
  @Column(name = "uploaded_at")
  private LocalDateTime uploadedAt;

  @Column(name = "processing_status", nullable = false)
  @Builder.Default
  private FileProcessingStatus processingStatus = FileProcessingStatus.RECEIVED;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "sourceFile")
  private List<Dataset> datasets;
}
