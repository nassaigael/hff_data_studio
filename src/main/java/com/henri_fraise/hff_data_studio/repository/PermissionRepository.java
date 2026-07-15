package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
	Optional<Permission> findByCode(String code);

	List<Permission> findByModule(String module);

	List<Permission> findByModuleOrderByCode(String module);

	boolean existsByCode(String code);

	boolean existsByCodeAndIdNot(String code, UUID id);

	@Query("SELECT p FROM Permission p WHERE p.code IN :codes")
	List<Permission> findByCodes(@Param("codes") List<String> codes);

	@Query("SELECT p FROM Permission p ORDER BY p.module, p.code")
	List<Permission> findAllOrdered();

	@Query("SELECT DISTINCT p.module FROM Permission p")
	List<String> findAllModules();


	@Query("SELECT p FROM Permission p JOIN p.categories c WHERE c.id = :categoryId")
	List<Permission> findPermissionsByCategoryId(@Param("categoryId") UUID categoryId);

	@Query("SELECT p FROM Permission p JOIN p.categories c WHERE c.label = :categoryLabel")
	List<Permission> findPermissionsByCategoryLabel(@Param("categoryLabel") String categoryLabel);

	@Query("SELECT COUNT(p) FROM Permission p JOIN p.categories c WHERE c.id = :categoryId")
	long countPermissionsByCategoryId(@Param("categoryId") UUID categoryId);
}
