package com.henri_fraise.hff_data_studio.security;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.ForbiddenException;
import com.henri_fraise.hff_data_studio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityExpression {

	private final UserService userService;

	public boolean isOwner(UUID resourceUserId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		String email = authentication.getName();
		User currentUser = userService.getUserEntityByEmail(email);

		return currentUser.getId().equals(resourceUserId);
	}

	public boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}

	public boolean hasPermission(String permission) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(permission));
	}

	public boolean isOwnerOrAdmin(UUID resourceUserId) {
		return isOwner(resourceUserId) || isAdmin();
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ForbiddenException("Authentication required");
		}

		String email = authentication.getName();
		return userService.getUserEntityByEmail(email);
	}
}