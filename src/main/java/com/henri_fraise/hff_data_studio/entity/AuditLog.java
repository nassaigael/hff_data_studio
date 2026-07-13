package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID id;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "concerned_entity")
    private String concernedEntity;

    @Column(name = "entity_id")
    private Long entityId;

    @CreationTimestamp
    @Column(name = "action_date", nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}