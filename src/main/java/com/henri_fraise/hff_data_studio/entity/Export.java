package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(name = "export_format", nullable = false)
    private ExportFormat exportFomat;

    @CreationTimestamp
    @Column(name = "export_at", nullable = false, updatable = false)
    private LocalDateTime exportAt;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private AnalysisExecution execution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}