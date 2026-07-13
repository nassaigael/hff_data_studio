package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.request.UserCreationRequest;
import com.henri_fraise.hff_data_studio.dto.response.UserResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final UserCategoryMapper categoryMapper;
	private final BCryptPasswordEncoder passwordEncoder;

	public UserResponse toResponse(User user) {
		if (user == null)
			return null;

		return UserResponse.builder()
				.userId(user.getId())
				.lastName(user.getLastName())
				.firstName(user.getFirstName())
				.email(user.getEmail())
				.isActive(user.getIsActive())
				.createdAt(user.getCreatedAt())
				.lastLogin(user.getLastLogin())
				.category(categoryMapper.toResponse(user.getCategory())
						.build();
	}

	public User toEntity(UserCreationRequest request, UserCategory category) {
		if (request == null)
			return null;

		return User.builder()
				.lastName(request.getLastName())
				.firstName(request.getFirstName())
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getNewPassword()))
				.isActive(true)
				.category(category)
				.build();
	}

	public void updateEntity(User user, UserCreationRequest request, UserCategory category) {
		if (request == null || user == null)
			return;
		if (request.getLastName() != null)
			user.setLastName(request.getLastName());
		if (request.getFirstName() != null)
				user.setFirstName(request.getFirstName());
		if (request.getEmail() != null)
				user.setEmail(request.getEmail());
		if (request.getIsActive() != null)
			user.setIsActive(request.getIsActive());
		if (category != null)
				user.setCategory(category);
		if (request.getNewPassword() != null && !request.getNewPassword().isEmpty())
			user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
	}
}
