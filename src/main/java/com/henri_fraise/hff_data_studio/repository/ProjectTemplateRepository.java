package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ProjectTemplate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectTemplateRepository extends JpaRepository<ProjectTemplate, UUID> {

	List<ProjectTemplate> findByUserIdOrderByUsageCountDesc(UUID userId);

	List<ProjectTemplate> findByIsPublicTrueOrderByUsageCountDesc();

	List<ProjectTemplate> findByCategoryOrderByUsageCountDesc(String category);
}