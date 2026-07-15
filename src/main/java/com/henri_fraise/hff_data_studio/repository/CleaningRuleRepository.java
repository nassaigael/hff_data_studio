package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.CleaningRule;
import com.henri_fraise.hff_data_studio.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CleaningRuleRepository extends JpaRepository<CleaningRule, UUID> {

	List<CleaningRule> findByColumnDatasetIdOrderByExecutionOrderAsc(UUID datasetId);

	List<CleaningRule> findByColumnIdOrderByExecutionOrderAsc(UUID columnId);

	List<CleaningRule> findByColumnIdAndIsActiveTrueOrderByExecutionOrderAsc(UUID columnId);

	List<CleaningRule> findByColumnDatasetIdAndIsActiveTrue(UUID datasetId);

	List<CleaningRule> findByRuleType(RuleType ruleType);

	List<CleaningRule> findByIsActiveTrue();

	long countByColumnId(UUID columnId);

	long countByColumnDatasetId(UUID datasetId);

	long countByColumnIdAndIsActiveTrue(UUID columnId);

	@Modifying
	@Transactional
	@Query("UPDATE CleaningRule r SET r.isActive = :is_active WHERE r.id  = :rule_id")
	void updateRuleStatus(
			@Param("rule_id") UUID ruleId, @Param("is_active") Boolean isActive);

	@Modifying
	@Transactional
	@Query("UPDATE CleaningRule r SET r.executionOrder = :order WHERE r.id = :rule_id")
	void updateExecutionOrder(
			@Param("rule_id") UUID ruleId, @Param("order") Integer order);

	@Modifying
	@Transactional
	@Query("UPDATE CleaningRule r SET r.isActive = false WHERE r.column.id = :column_id")
	void deactivateAllRulesForColumn(@Param("column_id") UUID columnId);

	@Query("SELECT r FROM  CleaningRule  r WHERE r.column.dataset.id = :dataset_id AND r.isActive = true ORDER BY r.executionOrder ASC ")
	List<CleaningRule> findActiveRulesByDatasetIdOrdered(@Param("dataset_id") UUID datasetId);

	@Query("SELECT r FROM CleaningRule r WHERE r.column.dataset.id = :dataset_id AND r.executionOrder = :order")
	List<CleaningRule> findByDatasetIdAndExecutionOrder(@Param("dataset_id") UUID datasetId, @Param("order") Integer order);

	@Query("SELECT MAX(r.executionOrder) FROM CleaningRule  r WHERE r.column.dataset.id = :dataset_id")
	Integer findMaxExecutionOrderByDatasetId(@Param("dataset_id") UUID datasetId);

	@Query("SELECT r FROM CleaningRule r WHERE r.column.id = :column_id AND r.ruleType = :rule_type")
	List<CleaningRule> findByColumnIdAndRuleType(@Param("column_id") UUID columnId, @Param("rule_type") RuleType ruleType);

	@Query("SELECT r FROM CleaningRule  r WHERE  r.column.dataset.id = :dataset_id AND r.ruleType = :rule_type")
	List<CleaningRule> findByDatasetIdAndRuleType(@Param("dataset_id") UUID datasetId, @Param("rule_type") RuleType ruleType);
}
