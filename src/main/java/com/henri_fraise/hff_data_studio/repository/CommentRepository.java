package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

	Page<Comment> findByEntityTypeAndEntityIdAndParentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(
			String entityType, UUID entityId, Pageable pageable);

	List<Comment> findByParentIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID parentId);

	long countByEntityTypeAndEntityIdAndIsDeletedFalse(String entityType, UUID entityId);

	@Query("SELECT c FROM Comment c WHERE c.entityType = :type AND c.entityId = :id " +
			"AND c.isDeleted = false ORDER BY c.createdAt DESC")
	List<Comment> findAllActive(@Param("type") String entityType, @Param("id") UUID entityId);

	long countByAuthorId(UUID authorId);
}