package com.henri_fraise.hff_data_studio.service;

import com.henri_fraise.hff_data_studio.dto.request.FavoriteRequest;
import com.henri_fraise.hff_data_studio.dto.response.FavoriteResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserActivity;
import com.henri_fraise.hff_data_studio.entity.UserFavorite;
import com.henri_fraise.hff_data_studio.repository.UserActivityRepository;
import com.henri_fraise.hff_data_studio.repository.UserFavoriteRepository;
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
public class UserActivityService {

	private final UserFavoriteRepository favoriteRepository;
	private final UserActivityRepository activityRepository;
	private final SecurityUtils securityUtils;

	@Transactional
	public FavoriteResponse toggleFavorite(FavoriteRequest request) {
		User user = securityUtils.getCurrentUser();
		var existing = favoriteRepository.findByUserIdAndEntityTypeAndEntityId(
				user.getId(), request.getEntityType(), request.getEntityId());

		if (existing.isPresent()) {
			favoriteRepository.delete(existing.get());
			return null;
		}

		UserFavorite favorite = UserFavorite.builder()
				.user(user)
				.entityType(request.getEntityType())
				.entityId(request.getEntityId())
				.displayName(request.getDisplayName())
				.build();

		UserFavorite saved = favoriteRepository.save(favorite);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<FavoriteResponse> listFavorites() {
		return favoriteRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId())
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public boolean isFavorite(String entityType, UUID entityId) {
		return favoriteRepository.existsByUserIdAndEntityTypeAndEntityId(
				securityUtils.getCurrentUserId(), entityType, entityId);
	}

	@Transactional
	public void track(String action, String entityType, UUID entityId, String details) {
		try {
			User user = securityUtils.getCurrentUser();
			UserActivity activity = UserActivity.builder()
					.user(user)
					.action(action)
					.entityType(entityType)
					.entityId(entityId)
					.details(details)
					.build();
			activityRepository.save(activity);
		} catch (Exception e) {
			log.warn("Failed to track activity: {}", e.getMessage());
		}
	}

	private FavoriteResponse toResponse(UserFavorite favorite) {
		return FavoriteResponse.builder()
				.favoriteId(favorite.getId())
				.entityType(favorite.getEntityType())
				.entityId(favorite.getEntityId())
				.displayName(favorite.getDisplayName())
				.createdAt(favorite.getCreatedAt())
				.build();
	}
}