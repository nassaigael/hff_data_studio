package com.henri_fraise.hff_data_studio.security;

import com.henri_fraise.hff_data_studio.entity.Permission;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

	private UUID userId;
	private String email;
	private String password;
	private String firstName;
	private String lastName;
	private Boolean isActive;
	private UserRole role;
	private List<String> permissions;

	public static UserPrincipal create(User user) {
		List<String> permissions = user.getCategory() != null && user.getCategory().getPermissions() != null
				? user.getCategory().getPermissions().stream()
				.map(Permission::getCode)
				.collect(Collectors.toList())
				: List.of();

		return UserPrincipal.builder()
				.userId(user.getId())
				.email(user.getEmail())
				.password(user.getPasswordHash())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.isActive(user.getIsActive())
				.role(user.getRole())
				.permissions(permissions)
				.build();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> authorities = permissions.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		if (role != null) {
			authorities.add(new SimpleGrantedAuthority(role.name()));
		}

		return authorities;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return isActive != null && isActive;
	}
}