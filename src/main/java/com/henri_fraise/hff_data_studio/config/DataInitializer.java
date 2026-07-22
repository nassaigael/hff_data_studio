package com.henri_fraise.hff_data_studio.config;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.repository.UserCategoryRepository;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final UserCategoryRepository categoryRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Override
  public void run(String @NonNull ... args) {
    initializeAdminUser();
  }

  private void initializeAdminUser() {
    if (userRepository.count() == 0) {
      log.info("👤 Creating first admin user...");

      UserCategory adminCategory =
          categoryRepository
              .findByLabel("ADMIN")
              .orElseGet(
                  () -> {
                    UserCategory newCategory =
                        UserCategory.builder()
                            .label("ADMIN")
                            .description("Administrator")
                            .accessLevel(5)
                            .build();
                    return categoryRepository.save(newCategory);
                  });

      User adminUser =
          User.builder()
              .lastName("Administrator")
              .firstName("System")
              .email("admin@hff.re")
              .passwordHash(passwordEncoder.encode("123456"))
              .isActive(true)
              .category(adminCategory)
              .build();

      userRepository.save(adminUser);
      log.info("User default ADMIN created successfully");
    }
  }
}
