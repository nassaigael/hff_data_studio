package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.CategoryCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserCategoryResponse;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserCategoryMapper {

	private final PermissionMapper permissionMapper;

	public UserCategoryResponse toResponse(UserCategory category) {
		if (category == null)
			return null;

		return UserCategoryResponse.builder()
				.categoryId(category.getId())
				.label(category.getLabel())
				.description(category.getDescription())
				.accessLevel(category.getAccessLevel())
				.permissions(
						category.getPermissions() != null ? category.getPermissions().stream()
								.map(permissionMapper::toResponse)
								.collect(Collectors.toList())
								: null
				)
				.build();
	}

	public UserCategory toEntity(CategoryCreationRequest request) {
		if (request == null)
			return null;

		return UserCategory.builder()
				.label(request.getLabel())
				.description(request.getDescription())
				.accessLevel(request.getAccessLevel())
				.build();
	}
}
