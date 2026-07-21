package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CleaningRuleRepository extends JpaRepository<CleaningRule, UUID> {

  List<CleaningRule> findByColumn_DatasetIdOrderByExecutionOrderAsc(UUID datasetId);

  List<CleaningRule> findByColumnIdOrderByExecutionOrderAsc(UUID columnId);

  List<CleaningRule> findByColumnIdAndIsActiveTrueOrderByExecutionOrderAsc(UUID columnId);

  List<CleaningRule> findByRuleType(RuleType ruleType);

  List<CleaningRule> findByIsActiveTrue();

  long countByColumnId(UUID columnId);

  long countByColumn_DatasetId(UUID datasetId);

  long countByColumnIdAndIsActiveTrue(UUID columnId);

  @Modifying
  @Transactional
  @Query("UPDATE CleaningRule r SET r.isActive = :isActive WHERE r.id = :ruleId")
  void updateRuleStatus(@Param("ruleId") UUID ruleId, @Param("isActive") Boolean isActive);

  @Modifying
  @Transactional
  @Query("UPDATE CleaningRule r SET r.executionOrder = :order WHERE r.id = :ruleId")
  void updateExecutionOrder(@Param("ruleId") UUID ruleId, @Param("order") Integer order);

  @Modifying
  @Transactional
  @Query("UPDATE CleaningRule r SET r.isActive = false WHERE r.column.id = :columnId")
  void deactivateAllRulesForColumn(@Param("columnId") UUID columnId);

  @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.isActive = true ORDER BY r.executionOrder ASC")
  List<CleaningRule> findActiveRulesByDatasetIdOrdered(@Param("datasetId") UUID datasetId);

  @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.executionOrder = :order")
  List<CleaningRule> findByDatasetIdAndExecutionOrder(@Param("datasetId") UUID datasetId, @Param("order") Integer order);

  @Query("SELECT MAX(r.executionOrder) FROM CleaningRule r WHERE r.column.dataset.id = :datasetId")
  Integer findMaxExecutionOrderByDatasetId(@Param("datasetId") UUID datasetId);

  @Query("SELECT r FROM CleaningRule r WHERE r.column.id = :columnId AND r.ruleType = :ruleType")
  List<CleaningRule> findByColumnIdAndRuleType(@Param("columnId") UUID columnId, @Param("ruleType") RuleType ruleType);

  @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.ruleType = :ruleType")
  List<CleaningRule> findByDatasetIdAndRuleType(@Param("datasetId") UUID datasetId, @Param("ruleType") RuleType ruleType);

  @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId ORDER BY r.executionOrder ASC")
  List<CleaningRule> findRulesByDatasetIdOrdered(@Param("datasetId") UUID datasetId);

  @Query("SELECT COUNT(r) FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.ruleType = :ruleType AND r.isActive = true")
  long countActiveRulesByDatasetIdAndRuleType(@Param("datasetId") UUID datasetId, @Param("ruleType") RuleType ruleType);

  @Query("SELECT COUNT(r) FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.isActive = true")
  long countActiveRulesByDatasetId(@Param("datasetId") UUID datasetId);

  @Query("SELECT COUNT(r) FROM CleaningRule r WHERE r.ruleType = :ruleType")
  long countByRuleType(@Param("ruleType") RuleType ruleType);

  @Query("SELECT COUNT(r) FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.ruleType = :ruleType")
  long countByDatasetIdAndRuleType(@Param("datasetId") UUID datasetId, @Param("ruleType") RuleType ruleType);

  @Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :datasetId AND r.isActive = true AND r.ruleType = :ruleType ORDER BY r.executionOrder ASC")
  List<CleaningRule> findActiveRulesByDatasetIdAndRuleType(@Param("datasetId") UUID datasetId, @Param("ruleType") RuleType ruleType);
}