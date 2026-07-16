package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.FileFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExportRepository extends JpaRepository<Export, UUID> {

	Page<Export> findByUserIdOrderByExportedAt(UUID userId, Pageable pageable);

	Page<Export> findByExecutionIdOrderByExportedAt(UUID executionId, Pageable pageable);

	Page<Export> findByFileFormatOrderByExportedAtDesc(FileFormat fileFormat, Pageable pageable);

	List<Export> findByUserIdOrderByExportedAtDesc(UUID userId);


}
