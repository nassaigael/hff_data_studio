package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.ProjectMember;
import com.henri_fraise.hff_data_studio.enums.MemberRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

	List<ProjectMember> findByProjectId(UUID projectId);

	List<ProjectMember> findByUserId(UUID userId);

	Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

	boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

	long countByProjectId(UUID projectId);

	@Query("SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId")
	List<UUID> findProjectIdsByUserId(@Param("userId") UUID userId);

	@Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.role IN :roles")
	List<ProjectMember> findByProjectIdAndRoles(@Param("projectId") UUID projectId,
	                                            @Param("roles") List<MemberRole> roles);

	void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}