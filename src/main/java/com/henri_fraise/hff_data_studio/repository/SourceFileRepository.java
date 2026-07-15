package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SourceFileRepository extends JpaRepository<SourceFile, UUID> {
  Page<SourceFile> findByProject_Id(UUID projectId, Pageable pageable);

  Page<SourceFile> findByUser_Id(UUID userId, Pageable pageable);

  Page<SourceFile> findByProject_IdAndUser_Id(UUID projectId, UUID userId, Pageable pageable);

  List<SourceFile> findByProjectId(UUID projectId);

  List<SourceFile> findByUserId(UUID userId);

  Page<SourceFile> findByProcessingStatus(FileProcessingStatus status, Pageable pageable);

  List<SourceFile> findByProcessingStatus(FileProcessingStatus status);

  Page<SourceFile> findByFileNameContainingIgnoreCase(String fileName, Pageable pageable);

  long countByProjectId(UUID projectId);

  long countByUserId(UUID userId);

  long countByProcessingStatus(FileProcessingStatus status);

  long countByProjectIdAndProcessingStatus(UUID projectId, FileProcessingStatus status);

  @Modifying
  @Transactional
  @Query("UPDATE  SourceFile  f SET f.processingStatus = :status WHERE f.id = :file_id")
  void updateProcessingStatus(
      @Param("file_id") UUID fileId, @Param("status") FileProcessingStatus status);

  @Modifying
  @Transactional
  @Query(
      "UPDATE SourceFile f SET f.processingStatus = :status WHERE f.id = :file_id AND"
          + " f.processingStatus = :current_status")
  void updateProcessingStatusIfCurrent(
      @Param("file_id") UUID fileId,
      @Param("status") FileProcessingStatus status,
      @Param("current_status") FileProcessingStatus currentStatus);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.project_id = :project_id AND (:search_term IS NULL OR"
          + " LOWER(f.fileName) LIKE LOWER(CONCAT('%', :search_term, '%')))")
  Page<SourceFile> searchProjectFiles(
      @Param("project_id") UUID projectId,
      @Param("search_term") String searchTerm,
      Pageable pageable);

  @Query("SELECT f FROM SourceFile f WHERE f.processingStatus IN :statuses")
  List<SourceFile> findByProcessingStatusIn(@Param("statuses") List<FileProcessingStatus> statuses);

  @Query("SELECT f FROM SourceFile  f WHERE f.uploadedAt < :date AND f.processingStatus = :status")
  List<SourceFile> findOldFilesByStatus(
      @Param("date") LocalDateTime date, @Param("status") FileProcessingStatus status);

  @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f WHERE f.project_id = :project_id")
  Long sumFileSizesByProjectId(@Param("project_id") UUID projectId);

  @Query("SELECT COUNT(f) FROM SourceFile  f WHERE f.uploadedAt BETWEEN :start_date AND :end_date")
  long countFilesUploadedBetween(
      @Param("start_date") LocalDateTime startDate, @Param("end_date") LocalDateTime endDate);
}
