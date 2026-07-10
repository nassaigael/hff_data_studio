package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.FileType;
import com.henri_fraise.hff_data_studio.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

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
    private FileType fileType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "processing_status", nullable = false)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.RECEIVED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sourceFile")
    private List<Dataset> datasets;

}