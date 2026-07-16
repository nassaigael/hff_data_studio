package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.dto.request.LoginRequest;
import com.henri_fraise.hff_data_studio.dto.request.RefreshTokenRequest;
import com.henri_fraise.hff_data_studio.dto.response.TokenResponse;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.InvalidCredentialsException;
import com.henri_fraise.hff_data_studio.exception.TokenExpiredException;
import com.henri_fraise.hff_data_studio.exception.TokenInvalidException;
import com.henri_fraise.hff_data_studio.exception.UserDisabledException;
import com.henri_fraise.hff_data_studio.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;
	private final UserService userService;

	@Transactional
	public TokenResponse login(LoginRequest request) {
		try {
			// Authenticate user
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);

			// Get user details
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			User user = userService.getUserEntityByEmail(request.getEmail());

			// Check if user is active
			if (!user.getIsActive()) {
				throw new UserDisabledException("Account is disabled");
			}

			// Update last login
			userService.updateLastLogin(user.getId());

			// Generate tokens
			String accessToken = jwtService.generateToken(userDetails);
			String refreshToken = jwtService.generateRefreshToken(userDetails);

			log.info("User logged in successfully: {}", request.getEmail());

			return TokenResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.expiresIn(jwtService.getAccessTokenExpiration())
					.tokenType("Bearer")
					.build();

		} catch (Exception ex) {
			log.warn("Login failed for {}: {}", request.getEmail(), ex.getMessage());
			throw new InvalidCredentialsException("Invalid email or password");
		}
	}

	public TokenResponse refreshToken(RefreshTokenRequest request) {
		try {
			String refreshToken = request.getRefreshToken();

			// Validate refresh token
			if (!jwtService.isTokenValid(refreshToken)) {
				throw new TokenInvalidException("Invalid refresh token");
			}

			// Extract username
			String username = jwtService.extractUsername(refreshToken);
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			// Generate new access token
			String newAccessToken = jwtService.generateToken(userDetails);

			log.info("Token refreshed for user: {}", username);

			return TokenResponse.builder()
					.accessToken(newAccessToken)
					.refreshToken(refreshToken)
					.expiresIn(jwtService.getAccessTokenExpiration())
					.tokenType("Bearer")
					.build();

		} catch (TokenInvalidException | TokenExpiredException ex) {
			throw ex;
		} catch (Exception ex) {
			log.error("Error refreshing token: {}", ex.getMessage(), ex);
			throw new TokenInvalidException("Failed to refresh token");
		}
	}

	@Transactional
	public void logout(String token) {
		try {
			log.info("User logged out");
		} catch (Exception ex) {
			log.error("Error during logout: {}", ex.getMessage(), ex);
		}
	}

	public User getCurrentUser(Authentication authentication) {
		String email = authentication.getName();
		return userService.getUserEntityByEmail(email);
	}
}