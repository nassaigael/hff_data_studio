package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.TenantUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {

	List<TenantUser> findByUserId(UUID userId);

	List<TenantUser> findByTenantId(UUID tenantId);

	Optional<TenantUser> findByTenantIdAndUserId(UUID tenantId, UUID userId);

	boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

	long countByTenantId(UUID tenantId);
}