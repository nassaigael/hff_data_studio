package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Export;
import com.henri_fraise.hff_data_studio.enums.FileFormat;
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

	Page<Export> findByUserIdOrderByExportedAt(UUID userId, Pageable pageable);

	Page<Export> findByExecutionIdOrderByExportedAt(UUID executionId, Pageable pageable);

	Page<Export> findByFileFormatOrderByExportedAtDesc(FileFormat fileFormat, Pageable pageable);

	List<Export> findByUserIdOrderByExportedAtDesc(UUID userId);

	long countByUserId(UUID userId);

	long countByExecutionId(UUID executionId);

	long countByFileFormat(FileFormat fileFormat);

	long countByUserIdAndFileFormat(UUID userId, FileFormat fileFormat);

	@Query("SELECT e FROM Export e WHERE e.exportedAt BETWEEN :start_date AND :end_date")
	List<Export> findByExportedDateRange(
			@Param("start_date") LocalDateTime startDate,
			@Param("end_date") LocalDateTime endDate
	);

	@Query("SELECT e FROM Export e WHERE e.user.id = :user_id AND " +
			"(:search_term IS NULL OR LOWER(e.fileName) LIKE LOWER(CONCAT('%', :search_term, '%') ) OR " +
			"LOWER(e.fileFormat) LIKE LOWER(CONCAT('%', :search_term, '%') ) )")
	Page<Export> searchUserExports(
				@Param("user_id") UUID userId,
				@Param("search_term") String searchTerm,
				Pageable pageable);

	@Query("SELECT COUNT(e) FROM Export e WHERE e.exportedAt BETWEEN :start_date AND :end_date")
	long countExportsBetween(
			@Param("start_date") LocalDateTime startDate,
			@Param("end_date") LocalDateTime endDate
	);

	@Query("SELECT e.fileFormat, COUNT(e) FROM Export e GROUP BY e.fileFormat")
	List<Object[]> countExportsByFileFormat();

	@Query("SELECT e FROM Export e WHERE e.exportedAt < :date")
	List<Export> findOldExports(@Param("date") LocalDateTime date);



}
