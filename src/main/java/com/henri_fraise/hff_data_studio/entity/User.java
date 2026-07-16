package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_id")
  private UUID id;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String passwordHash;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "category_id", nullable = false)
  private UserCategory category;

  @OneToMany(mappedBy = "creator")
  private List<Project> projects;

  @OneToMany(mappedBy = "user")
  private List<SourceFile> sourceFiles;

  @OneToMany(mappedBy = "user")
  private List<AnalysisExecution> analysisExecutions;

  @OneToMany(mappedBy = "user")
  private List<Export> exports;

  @OneToMany(mappedBy = "user")
  private List<AuditLog> auditLogs;

  @OneToMany(mappedBy = "user")
  private List<CleaningHistory> cleaningHistories;
}
