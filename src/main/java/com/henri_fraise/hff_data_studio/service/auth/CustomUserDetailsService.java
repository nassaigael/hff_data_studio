package com.henri_fraise.hff_data_studio.service.auth;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import com.henri_fraise.hff_data_studio.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public @NonNull UserDetails loadUserByUsername(@NonNull String email)
          throws UsernameNotFoundException {
    User user = userRepository.findByEmailWithPermissions(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with email: " + email));

    if (user.getCategory() != null && user.getCategory().getPermissions() != null) {
      user.getCategory().getPermissions().size();
    }

    return UserPrincipal.create(user);
  }
}