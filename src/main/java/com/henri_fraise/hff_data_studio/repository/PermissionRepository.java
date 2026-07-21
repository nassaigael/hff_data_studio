package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

  Optional<Permission> findByCode(String code);

  List<Permission> findByModule(String module);

  List<Permission> findByModuleIn(List<String> modules);

  List<Permission> findByCodeIn(List<String> codes);

  List<Permission> findByIsActiveTrue();

  boolean existsByCode(String code);

  @Query("SELECT p FROM Permission p JOIN p.userCategories c WHERE c.id = :categoryId")
  List<Permission> findPermissionsByCategoryId(@Param("categoryId") UUID categoryId);

  @Query("SELECT p FROM Permission p JOIN p.userCategories c WHERE c.label = :categoryLabel")
  List<Permission> findPermissionsByCategoryLabel(@Param("categoryLabel") String categoryLabel);

  @Query("SELECT COUNT(p) FROM Permission p JOIN p.userCategories c WHERE c.id = :categoryId")
  long countPermissionsByCategoryId(@Param("categoryId") UUID categoryId);

  @Query("SELECT p.code FROM Permission p JOIN p.userCategories c WHERE c.id = :categoryId")
  List<String> findPermissionCodesByCategoryId(@Param("categoryId") UUID categoryId);
}
