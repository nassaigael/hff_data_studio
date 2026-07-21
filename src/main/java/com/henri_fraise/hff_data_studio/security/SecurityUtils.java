package com.henri_fraise.hff_data_studio.security;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.exception.UnauthorizedException;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

  private final UserRepository userRepository;

  public static final List<String> ADMIN_ROLES = List.of("ADMIN");
  public static final List<String> ANALYST_ROLES = List.of("ADMIN", "DATA_ANALYST");
  public static final List<String> CONSULTANT_ROLES =
      List.of("ADMIN", "DATA_ANALYST", "CONSULTANT");
  public static final List<String> ALL_ROLES =
      List.of("ADMIN", "DATA_ANALYST", "CONSULTANT", "INVITE");

  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public UserDetails getCurrentUserDetails() {
    Authentication authentication = getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new UnauthorizedException("User is not authenticated");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      return (UserDetails) principal;
    }
    throw new UnauthorizedException("Invalid authentication principal");
  }

  public String getCurrentUsername() {
    UserDetails userDetails = getCurrentUserDetails();
    return userDetails.getUsername();
  }

  public UUID getCurrentUserId() {
    String username = getCurrentUsername();
    return userRepository
        .findByEmail(username)
        .map(User::getId)
        .orElseThrow(() -> new UnauthorizedException("User not found: " + username));
  }

  public User getCurrentUser() {
    String username = getCurrentUsername();
    return userRepository
        .findByEmail(username)
        .orElseThrow(() -> new UnauthorizedException("User not found: " + username));
  }

  public String getCurrentUserEmail() {
    return getCurrentUsername();
  }

  public boolean isAuthenticated() {
    Authentication authentication = getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  public boolean hasRole(String role) {
    Authentication authentication = getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(role));
  }

  public boolean hasAnyRole(List<String> roles) {
    Authentication authentication = getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> roles.contains(authority.getAuthority()));
  }

  public boolean hasPermission(String permission) {
    Authentication authentication = getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(permission));
  }

  public boolean hasAnyPermission(List<String> permissions) {
    Authentication authentication = getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> permissions.contains(authority.getAuthority()));
  }

  public boolean isAdmin() {
    return hasAnyRole(ADMIN_ROLES);
  }

  public boolean isDataAnalyst() {
    return hasAnyRole(ANALYST_ROLES);
  }

  public boolean isConsultant() {
    return hasAnyRole(CONSULTANT_ROLES);
  }

  public boolean isCurrentUser(UUID userId) {
    return getCurrentUserId().equals(userId);
  }

  public boolean isCurrentUserOrAdmin(UUID userId) {
    return isAdmin() || isCurrentUser(userId);
  }

  public boolean isCurrentUserOrDataAnalyst(UUID userId) {
    return isDataAnalyst() || isCurrentUser(userId);
  }

  public void validateCurrentUserAccess(UUID userId) {
    if (!isCurrentUserOrAdmin(userId)) {
      throw new UnauthorizedException("You do not have permission to access this resource");
    }
  }

  public void validateAdminAccess() {
    if (!isAdmin()) {
      throw new UnauthorizedException("Admin access required");
    }
  }

  public void validateDataAnalystAccess() {
    if (!isDataAnalyst()) {
      throw new UnauthorizedException("Data analyst access required");
    }
  }

  public void validatePermission(String permission) {
    if (!hasPermission(permission)) {
      throw new UnauthorizedException("Permission required: " + permission);
    }
  }

  public String getCurrentUserDisplayName() {
    User user = getCurrentUser();
    return user.getFirstName() + " " + user.getLastName();
  }

  public String getClientIpAddress() {
    return null;
  }

  public String getSessionId() {
    Authentication authentication = getAuthentication();
    return authentication != null ? authentication.getName() : null;
  }

  public boolean isTokenValid(String token) {
    return token != null && !token.isEmpty();
  }

  public String extractTokenFromHeader(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }

  public void clearContext() {
    SecurityContextHolder.clearContext();
  }
}
