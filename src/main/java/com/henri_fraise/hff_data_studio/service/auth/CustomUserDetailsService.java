package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

		List<SimpleGrantedAuthority> authorities = user.getCategory().getPermissions().stream()
				.map(permission -> new SimpleGrantedAuthority(permission.getCode()))
				.collect(Collectors.toList());

		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getCategory().getLabel()));

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPasswordHash(),
				user.getIsActive(),
				true,
				true,
				true,
				authorities
		);
	}
}