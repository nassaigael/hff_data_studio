package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "permission")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "permission_id")
  private UUID id;

  @Column(name = "code", length = 80, nullable = false, unique = true)
  private String code;

  @Column(name = "label", length = 150, nullable = false)
  private String label;

  @Column(name = "module", length = 60, nullable = false)
  private String module;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @ManyToMany(mappedBy = "permissions")
  private List<UserCategory> userCategories;
}
