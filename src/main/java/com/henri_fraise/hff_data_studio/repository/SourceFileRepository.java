package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.enums.FileType;
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
public interface SourceFileRepository extends JpaRepository<SourceFile, UUID> {

  List<SourceFile> findByProjectIdOrderByUploadedAtDesc(UUID projectId);

  Page<SourceFile> findByProjectId(UUID projectId, Pageable pageable);

  List<SourceFile> findByUserIdOrderByUploadedAtDesc(UUID userId);

  List<SourceFile> findByProcessingStatus(FileProcessingStatus status);

  List<SourceFile> findByFileFormat(FileType fileFormat);

  List<SourceFile> findByUploadedAtBefore(LocalDateTime date);

  List<SourceFile> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  long countByProjectId(UUID projectId);

  long countByUserId(UUID userId);

  long countByProcessingStatus(FileProcessingStatus status);

  long countByFileFormat(FileType fileFormat);

  boolean existsByFileNameAndProjectId(String fileName, UUID projectId);

  @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f")
  Long sumFileSizes();

  @Query("SELECT AVG(f.sizeBytes) FROM SourceFile f")
  Double averageFileSize();

  @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f WHERE f.project.id = :projectId")
  Long sumFileSizesByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND f.fileType = :fileType")
  List<SourceFile> findByProjectIdAndFileType(@Param("projectId") UUID projectId, @Param("fileType") FileType fileType);

  @Query("SELECT f FROM SourceFile f WHERE f.processingStatus IN :statuses")
  List<SourceFile> findByProcessingStatusIn(@Param("statuses") List<FileProcessingStatus> statuses);

  @Query("SELECT COUNT(f) FROM SourceFile f WHERE f.project.id = :projectId AND f.processingStatus = :status")
  long countByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") FileProcessingStatus status);

  @Query("SELECT f FROM SourceFile f ORDER BY f.uploadedAt DESC")
  List<SourceFile> findRecentFiles(Pageable pageable);

  @Query("SELECT f.fileFormat, COUNT(f) FROM SourceFile f GROUP BY f.fileType")
  List<Object[]> countGroupByFileType();

  @Query("SELECT f.processingStatus, COUNT(f) FROM SourceFile f GROUP BY f.processingStatus")
  List<Object[]> countGroupByProcessingStatus();
}