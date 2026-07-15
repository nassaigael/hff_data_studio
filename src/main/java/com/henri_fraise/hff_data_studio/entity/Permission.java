package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "permission_id")
  private UUID id;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "module", nullable = false)
  private String module;
}
