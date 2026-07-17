package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExportRepository extends JpaRepository<Export, UUID> {

	List<Export> findByExecutionId(UUID executionId);

	Page<Export> findByExecutionId(UUID executionId, Pageable pageable);

	List<Export> findByUserIdOrderByExportedAtDesc(UUID userId);

	List<Export> findByExportFormat(ExportFormat format);

	Page<Export> findByExportFormat(ExportFormat format, Pageable pageable);

	List<Export> findByExportedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

	List<Export> findByExportedAtBefore(LocalDateTime date);

	long countByUserId(UUID userId);

	long countByExportFormat(ExportFormat format);

	long countByExecutionId(UUID executionId);

	long countByExportedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

	@Query("SELECT SUM(e.fileSize) FROM Export e")
	Long sumFileSizes();

	@Query("SELECT AVG(e.fileSize) FROM Export e")
	Double averageFileSize();

	@Query("SELECT SUM(e.fileSize) FROM Export e WHERE e.user.id = :userId")
	Long sumFileSizesByUserId(@Param("userId") UUID userId);

	@Query("SELECT e.exportFormat, COUNT(e) FROM Export e GROUP BY e.exportFormat")
	List<Object[]> countGroupByFormat();

	@Query("SELECT e.user.id, COUNT(e) FROM Export e GROUP BY e.user.id")
	List<Object[]> countGroupByUser();

	@Query("SELECT e FROM Export e WHERE e.execution.id = :executionId AND e.exportFormat = :format")
	List<Export> findByExecutionIdAndFormat(@Param("executionId") UUID executionId, @Param("format") ExportFormat format);

	@Query("SELECT e FROM Export e ORDER BY e.exportedAt DESC")
	List<Export> findRecentExports(Pageable pageable);

	@Query("SELECT COUNT(e) FROM Export e WHERE e.execution.dataset.id = :datasetId")
	long countByDatasetId(@Param("datasetId") UUID datasetId);
}