package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

@Getter
@Setter
@Entity
@Table(name = "project")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "project_id")
  private UUID id;

  @Column(name = "project_name", nullable = false)
  private String projectName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ProjectStatus status = ProjectStatus.IN_PROGRESS;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_user_id", nullable = false)
  private User creator;

  @OneToMany(mappedBy = "project")
  private List<SourceFile> sourceFiles;

  @Formula("(SELECT COUNT(*) FROM source_file sf WHERE sf.project_id = project_id)")
  private Long fileCount;

  @Formula(
          "(SELECT COUNT(*) FROM dataset d "
                  + "JOIN source_file sf ON d.file_id = sf.file_id "
                  + "WHERE sf.project_id = project_id)")
  private Long datasetCount;
}