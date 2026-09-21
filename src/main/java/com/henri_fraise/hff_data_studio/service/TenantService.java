package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.TenantCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.TenantResponse;
import com.henri_fraise.hff_data_studio.entity.Tenant;
import com.henri_fraise.hff_data_studio.entity.TenantUser;
import com.henri_fraise.hff_data_studio.exception.ResourceAlreadyExistsException;
import com.henri_fraise.hff_data_studio.exception.ResourceNotFoundException;
import com.henri_fraise.hff_data_studio.repository.TenantRepository;
import com.henri_fraise.hff_data_studio.repository.TenantUserRepository;
import com.henri_fraise.hff_data_studio.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

	private final TenantRepository tenantRepository;
	private final TenantUserRepository tenantUserRepository;
	private final SecurityUtils securityUtils;

	@Transactional
	public TenantResponse create(TenantCreationRequest request) {
		if (tenantRepository.existsByCode(request.getCode())) {
			throw new ResourceAlreadyExistsException("Tenant code already exists");
		}

		Tenant tenant = Tenant.builder()
				.code(request.getCode())
				.name(request.getName())
				.domain(request.getDomain())
				.maxUsers(request.getMaxUsers())
				.maxStorageMb(request.getMaxStorageMb())
				.build();
		Tenant saved = tenantRepository.save(tenant);

		tenantUserRepository.save(TenantUser.builder()
				.tenant(saved)
				.user(securityUtils.getCurrentUser())
				.role("OWNER")
				.isOwner(true)
				.build());

		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public TenantResponse getCurrent() {
		String code = com.henri_fraise.hff_data_studio.tenant.TenantContext.getTenant();
		if (code == null) return null;
		return tenantRepository.findByCode(code).map(this::toResponse).orElse(null);
	}

	@Transactional(readOnly = true)
	public List<TenantResponse> listMine() {
		UUID userId = securityUtils.getCurrentUserId();
		return tenantUserRepository.findByUserId(userId).stream()
				.map(tu -> toResponse(tu.getTenant())).toList();
	}

	@Transactional
	public void addUser(UUID tenantId, UUID userId, String role) {
		Tenant tenant = tenantRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

		if (tenantUserRepository.existsByTenantIdAndUserId(tenantId, userId)) {
			throw new ResourceAlreadyExistsException("User already in tenant");
		}

		if (tenant.getMaxUsers() != null
				&& tenantUserRepository.countByTenantId(tenantId) >= tenant.getMaxUsers()) {
			throw new IllegalStateException("Tenant user limit reached");
		}

		tenantUserRepository.save(TenantUser.builder()
				.tenant(tenant)
				.user(securityUtils.getCurrentUser())
				.role(role)
				.build());
	}

	@Transactional
	public void removeUser(UUID tenantId, UUID userId) {
		tenantUserRepository.findByTenantIdAndUserId(tenantId, userId)
				.ifPresent(tenantUserRepository::delete);
	}

	private TenantResponse toResponse(Tenant t) {
		return TenantResponse.builder()
				.tenantId(t.getId())
				.code(t.getCode())
				.name(t.getName())
				.domain(t.getDomain())
				.isActive(t.getIsActive())
				.maxUsers(t.getMaxUsers())
				.maxStorageMb(t.getMaxStorageMb())
				.createdAt(t.getCreatedAt())
				.build();
	}
}