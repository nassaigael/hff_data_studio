package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @Column(name = "role", nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private UserRole role = UserRole.INVITE;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
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

  public boolean isAdmin() {
    return role != null && role.isAdmin();
  }

  public boolean isDataAnalyst() {
    return role != null && role.isDataAnalyst();
  }

  public boolean isConsultant() {
    return role != null && role.isConsultant();
  }

  public boolean isInvite() {
    return role != null && role.isInvite();
  }

  public boolean hasPermission(String permissionCode) {
    if (isAdmin()) {
      return true;
    }
    if (category == null || category.getPermissions() == null) {
      return false;
    }
    return category.getPermissions().stream()
            .anyMatch(p -> p.getCode().equals(permissionCode));
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }
}