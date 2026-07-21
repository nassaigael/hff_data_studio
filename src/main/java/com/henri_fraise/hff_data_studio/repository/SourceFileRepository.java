package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.SourceFile;
import com.henri_fraise.hff_data_studio.enums.FileProcessingStatus;
import com.henri_fraise.hff_data_studio.enums.FileType;
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

  Page<SourceFile> findByUserId(UUID userId, Pageable pageable);

  List<SourceFile> findByProjectIdOrderByUploadedAtDesc(UUID projectId);

  Page<SourceFile> findByProjectId(UUID projectId, Pageable pageable);

  List<SourceFile> findByUserIdOrderByUploadedAtDesc(UUID userId);

  List<SourceFile> findByProcessingStatus(FileProcessingStatus status);

  List<SourceFile> findByUploadedAtBefore(LocalDateTime date);

  List<SourceFile> findByUploadedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  List<SourceFile> findByFileType(FileType fileType);

  long countByProjectId(UUID projectId);

  long countByUserId(UUID userId);

  long countByFileType(FileType fileType);

  long countByProcessingStatus(FileProcessingStatus status);

  boolean existsByFileNameAndProjectId(String fileName, UUID projectId);

  @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f")
  Long sumFileSizes();

  @Query("SELECT AVG(f.sizeBytes) FROM SourceFile f")
  Double averageFileSize();

  @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f WHERE f.project.id = :projectId")
  Long sumFileSizesByProjectId(@Param("projectId") UUID projectId);

  @Query("SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND f.fileType = :fileType")
  List<SourceFile> findByProjectIdAndFileType(
      @Param("projectId") UUID projectId, @Param("fileType") FileType fileType);

  @Query("SELECT f FROM SourceFile f WHERE f.processingStatus IN :statuses")
  List<SourceFile> findByProcessingStatusIn(@Param("statuses") List<FileProcessingStatus> statuses);

  @Query(
      "SELECT COUNT(f) FROM SourceFile f WHERE f.project.id = :projectId AND f.processingStatus ="
          + " :status")
  long countByProjectIdAndStatus(
      @Param("projectId") UUID projectId, @Param("status") FileProcessingStatus status);

  @Query("SELECT f FROM SourceFile f ORDER BY f.uploadedAt DESC")
  List<SourceFile> findRecentFiles(Pageable pageable);

  @Query("SELECT f.fileType, COUNT(f) FROM SourceFile f GROUP BY f.fileType")
  List<Object[]> countGroupByFileType();

  @Query("SELECT f.processingStatus, COUNT(f) FROM SourceFile f GROUP BY f.processingStatus")
  List<Object[]> countGroupByProcessingStatus();

  @Query("SELECT f.project.id, COUNT(f) FROM SourceFile f GROUP BY f.project.id")
  List<Object[]> countGroupByProject();

  @Query("SELECT f.user.id, COUNT(f) FROM SourceFile f GROUP BY f.user.id")
  List<Object[]> countGroupByUser();

  @Modifying
  @Transactional
  @Query("UPDATE SourceFile f SET f.processingStatus = :status WHERE f.id = :fileId")
  void updateProcessingStatus(
      @Param("fileId") UUID fileId, @Param("status") FileProcessingStatus status);

  @Modifying
  @Transactional
  @Query("UPDATE SourceFile f SET f.processingStatus = :status WHERE f.project.id = :projectId")
  void updateProcessingStatusByProjectId(
      @Param("projectId") UUID projectId, @Param("status") FileProcessingStatus status);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND "
          + "(LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(f.fileType) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<SourceFile> searchProjectFiles(
      @Param("projectId") UUID projectId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query(
      "SELECT f FROM SourceFile f WHERE "
          + "LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(f.fileType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<SourceFile> searchAllFiles(@Param("searchTerm") String searchTerm);

  @Query(
      "SELECT f FROM SourceFile f WHERE "
          + "LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
          + "LOWER(f.fileType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<SourceFile> searchAllFiles(@Param("searchTerm") String searchTerm, Pageable pageable);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.uploadedAt BETWEEN :startDate AND :endDate AND"
          + " f.project.id = :projectId")
  List<SourceFile> findByProjectIdAndUploadedAtBetween(
      @Param("projectId") UUID projectId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(f) FROM SourceFile f WHERE f.uploadedAt BETWEEN :startDate AND :endDate")
  long countByUploadedAtBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(f) FROM SourceFile f WHERE f.uploadedAt >= :date")
  long countByUploadedAtAfter(@Param("date") LocalDateTime date);

  @Query("SELECT f FROM SourceFile f WHERE f.project.id = :projectId ORDER BY f.uploadedAt DESC")
  List<SourceFile> findRecentFilesByProjectId(
      @Param("projectId") UUID projectId, Pageable pageable);

  @Query("SELECT f FROM SourceFile f WHERE f.user.id = :userId ORDER BY f.uploadedAt DESC")
  List<SourceFile> findRecentFilesByUserId(@Param("userId") UUID userId, Pageable pageable);

  @Query("SELECT f FROM SourceFile f WHERE f.processingStatus = :status AND f.uploadedAt < :date")
  List<SourceFile> findOldFilesByStatus(
      @Param("status") FileProcessingStatus status, @Param("date") LocalDateTime date);

  @Query("SELECT f FROM SourceFile f WHERE f.processingStatus != :status")
  List<SourceFile> findByProcessingStatusNot(@Param("status") FileProcessingStatus status);

  @Query("SELECT f FROM SourceFile f WHERE f.sizeBytes > :minSize ORDER BY f.sizeBytes DESC")
  List<SourceFile> findFilesLargerThan(@Param("minSize") long minSize);

  @Query("SELECT f FROM SourceFile f WHERE f.sizeBytes < :maxSize ORDER BY f.sizeBytes ASC")
  List<SourceFile> findFilesSmallerThan(@Param("maxSize") long maxSize);

  @Query("SELECT AVG(f.sizeBytes) FROM SourceFile f WHERE f.project.id = :projectId")
  Double averageFileSizeByProjectId(@Param("projectId") UUID projectId);

  @Modifying
  @Transactional
  @Query("DELETE FROM SourceFile f WHERE f.project.id = :projectId")
  void deleteByProjectId(@Param("projectId") UUID projectId);

  @Modifying
  @Transactional
  @Query("DELETE FROM SourceFile f WHERE f.uploadedAt < :date")
  void deleteByUploadedAtBefore(@Param("date") LocalDateTime date);

  @Modifying
  @Transactional
  @Query("DELETE FROM SourceFile f WHERE f.id = :fileId AND f.project.id = :projectId")
  void deleteByFileIdAndProjectId(@Param("fileId") UUID fileId, @Param("projectId") UUID projectId);

  @Query(
      "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM SourceFile f WHERE f.fileName ="
          + " :fileName AND f.project.id = :projectId AND f.id != :fileId")
  boolean existsByFileNameAndProjectIdAndNotId(
      @Param("fileName") String fileName,
      @Param("projectId") UUID projectId,
      @Param("fileId") UUID fileId);

  @Query(
      "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM SourceFile f WHERE f.project.id"
          + " = :projectId AND f.processingStatus = :status")
  boolean existsByProjectIdAndStatus(
      @Param("projectId") UUID projectId, @Param("status") FileProcessingStatus status);

  @Query(
      "SELECT MIN(f.sizeBytes), MAX(f.sizeBytes), AVG(f.sizeBytes), COUNT(f) FROM SourceFile f"
          + " WHERE f.project.id = :projectId")
  Object[] getFileSizeStatisticsByProjectId(@Param("projectId") UUID projectId);

  @Query(
      "SELECT DATE(f.uploadedAt), COUNT(f) FROM SourceFile f GROUP BY DATE(f.uploadedAt) ORDER BY"
          + " DATE(f.uploadedAt) DESC")
  List<Object[]> countGroupByDate();

  @Query(
      "SELECT YEAR(f.uploadedAt), MONTH(f.uploadedAt), COUNT(f) FROM SourceFile f GROUP BY"
          + " YEAR(f.uploadedAt), MONTH(f.uploadedAt) ORDER BY YEAR(f.uploadedAt) DESC,"
          + " MONTH(f.uploadedAt) DESC")
  List<Object[]> countGroupByYearMonth();

  @Query("SELECT SUM(f.sizeBytes) FROM SourceFile f WHERE f.project.id = :projectId")
  Long sumFileSizeByProjectId(@Param("projectId") UUID projectId);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND "
          + "(LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  List<SourceFile> searchProjectFilesList(
      @Param("projectId") UUID projectId, @Param("searchTerm") String searchTerm);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND f.processingStatus = :status")
  List<SourceFile> findByProjectIdAndStatus(
      @Param("projectId") UUID projectId, @Param("status") FileProcessingStatus status);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND f.fileType = :fileType AND"
          + " f.processingStatus = :status")
  List<SourceFile> findByProjectIdAndFileTypeAndStatus(
      @Param("projectId") UUID projectId,
      @Param("fileType") FileType fileType,
      @Param("status") FileProcessingStatus status);

  @Query(
      "SELECT f FROM SourceFile f WHERE f.project.id = :projectId AND f.sizeBytes > :minSize ORDER"
          + " BY f.sizeBytes DESC")
  List<SourceFile> findLargeFilesByProjectId(
      @Param("projectId") UUID projectId, @Param("minSize") long minSize);
}
