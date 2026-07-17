package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.PredefinedAnalysis;
import com.henri_fraise.hff_data_studio.enums.AnalysisCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PredefinedAnalysisRepository extends JpaRepository<PredefinedAnalysis, UUID> {

	Page<PredefinedAnalysis> findByCategory(AnalysisCategory category, Pageable pageable);

	List<PredefinedAnalysis> findByCategory(AnalysisCategory category);

	Page<PredefinedAnalysis> findByAnalysisNameContainingIgnoreCase(String name, Pageable pageable);

	List<PredefinedAnalysis> findAllByOrderByCategoryAscAnalysisNameAsc();

	boolean existsByAnalysisName(String analysisName);

	boolean existsByReferenceScript(String referenceScript);

	long countByCategory(AnalysisCategory category);

	@Query("SELECT DISTINCT a.category FROM PredefinedAnalysis  a")
	List<AnalysisCategory> findAllCategories();

	@Query("SELECT a FROM PredefinedAnalysis a WHERE  a.category = :category AND " +
			"(:search_term IS NULL OR LOWER(a.analysisName) LIKE LOWER(CONCAT('%', :search_term, '%')) OR " +
			"LOWER(a.description) LIKE LOWER(CONCAT('%', :search_term, '%') ) )")
	Page<PredefinedAnalysis> searchByCategory(
			@Param("category") AnalysisCategory category,
			@Param("search_term") String searchTerm,
			Pageable pageable
	);

	@Query("SELECT DISTINCT a.category FROM PredefinedAnalysis  a")
	List<AnalysisCategory> findDistinctCategories();
}
