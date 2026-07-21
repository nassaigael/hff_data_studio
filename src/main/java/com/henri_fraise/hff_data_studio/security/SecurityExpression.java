package com.henri_fraise.hff_data_studio.security;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityExpression {

  private final UserRepository userRepository;

  public SecurityExpression() {
    this.userRepository = null;
  }

  public SecurityExpression(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public boolean isCurrentUser(UUID userId) {
    if (userRepository == null) {
      return false;
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    String email = authentication.getName();
    return userRepository
        .findByEmail(email)
        .map(User::getId)
        .map(id -> id.equals(userId))
        .orElse(false);
  }

  public boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
  }

  public boolean hasPermission(String permission) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals(permission));
  }
}
