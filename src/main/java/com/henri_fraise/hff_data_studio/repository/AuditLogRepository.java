package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AuditLog;
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
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  Page<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId, Pageable pageable);

  Page<AuditLog> findByConcernedEntityOrderByActionDateDesc(
      String concernedEntity, Pageable pageable);

  Page<AuditLog> findByActionContainingIgnoreCase(String action, Pageable pageable);

  Page<AuditLog> findByAction(String action, Pageable pageable);

  Page<AuditLog> findByConcernedEntityAndEntityId(
      String concernedEntity, UUID entityId, Pageable pageable);

  Page<AuditLog> findByActionAndActionDateBetween(
      String action, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  Page<AuditLog> findByUserIdAndActionDateBetween(
      UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  Page<AuditLog> findByUserIdAndAction(UUID userId, String action, Pageable pageable);

  Page<AuditLog> findByUserIdAndActionAndActionDateBetween(
      UUID userId,
      String action,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Pageable pageable);

  List<AuditLog> findByUserIdOrderByActionDateDesc(UUID userId);

  List<AuditLog> findByActionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  long countByUserId(UUID userId);

  long countByConcernedEntity(String concernedEntity);

  long countByAction(String action);

  long countByActionContainingIgnoreCase(String action);

  long countByActionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.actionDate BETWEEN :startDate AND :endDate ORDER BY"
          + " a.actionDate DESC")
  Page<AuditLog> findByActionDateBetween(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.actionDate BETWEEN :startDate AND"
          + " :endDate")
  List<AuditLog> findByUserIdAndDateRange(
      @Param("userId") UUID userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT a FROM AuditLog a WHERE a.concernedEntity = :entity AND a.entityId = :entityId")
  List<AuditLog> findByConcernedEntityAndEntityId(
      @Param("entity") String entity, @Param("entityId") UUID entityId);

  @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY COUNT(a) DESC")
  List<Object[]> countGroupByAction();

  @Query("SELECT a.action, COUNT(a) FROM AuditLog a GROUP BY a.action ORDER BY a.action")
  List<Object[]> countGroupByActionAlphabetical();

  @Query(
      "SELECT a.concernedEntity, COUNT(a) FROM AuditLog a GROUP BY a.concernedEntity ORDER BY"
          + " COUNT(a) DESC")
  List<Object[]> countGroupByEntity();

  @Query("SELECT a.user.id, COUNT(a) FROM AuditLog a GROUP BY a.user.id ORDER BY COUNT(a) DESC")
  List<Object[]> countGroupByUser();

  @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.actionDate BETWEEN :startDate AND :endDate")
  long countLogsBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT a FROM AuditLog a WHERE a.actionDate < :date")
  List<AuditLog> findOldAuditLogs(@Param("date") LocalDateTime date);

  @Query("SELECT a FROM AuditLog a WHERE a.actionDate < :date")
  Page<AuditLog> findOldAuditLogs(@Param("date") LocalDateTime date, Pageable pageable);

  @Query("SELECT DISTINCT a.action FROM AuditLog a ORDER BY a.action")
  List<String> findDistinctActions();

  @Query("SELECT DISTINCT a.concernedEntity FROM AuditLog a ORDER BY a.concernedEntity")
  List<String> findDistinctEntities();

  @Query(
      "SELECT DATE(a.actionDate) as date, COUNT(a) FROM AuditLog a WHERE a.actionDate BETWEEN"
          + " :startDate AND :endDate GROUP BY DATE(a.actionDate) ORDER BY date")
  List<Object[]> countGroupByDate(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT YEAR(a.actionDate) as year, MONTH(a.actionDate) as month, COUNT(a) FROM AuditLog a"
          + " GROUP BY YEAR(a.actionDate), MONTH(a.actionDate) ORDER BY year DESC, month DESC")
  List<Object[]> countGroupByMonth();

  @Query("SELECT a FROM AuditLog a ORDER BY a.actionDate DESC")
  List<AuditLog> findRecentLogs(Pageable pageable);

  @Modifying
  @Transactional
  @Query("DELETE FROM AuditLog a WHERE a.actionDate < :date")
  long deleteByActionDateBefore(@Param("date") LocalDateTime date);

  @Modifying
  @Transactional
  @Query("DELETE FROM AuditLog a WHERE a.user.id = :userId")
  void deleteByUserId(@Param("userId") UUID userId);

  @Modifying
  @Transactional
  @Query("DELETE FROM AuditLog a WHERE a.concernedEntity = :entity AND a.entityId = :entityId")
  void deleteByEntity(@Param("entity") String entity, @Param("entityId") UUID entityId);

  @Query(
      "SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action = :action ORDER BY"
          + " a.actionDate DESC")
  List<AuditLog> findByUserIdAndAction(
      @Param("userId") UUID userId, @Param("action") String action);

  boolean existsByUserId(UUID userId);

  boolean existsByConcernedEntityAndEntityId(String concernedEntity, UUID entityId);
}
