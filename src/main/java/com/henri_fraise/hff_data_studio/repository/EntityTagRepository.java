package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.EntityTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityTagRepository extends JpaRepository<EntityTag, UUID> {

	List<EntityTag> findByEntityTypeAndEntityId(String entityType, UUID entityId);

	List<EntityTag> findByTagId(UUID tagId);

	void deleteByTagIdAndEntityTypeAndEntityId(UUID tagId, String entityType, UUID entityId);

	long countByTagId(UUID tagId);
}