package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ProjectStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
}
