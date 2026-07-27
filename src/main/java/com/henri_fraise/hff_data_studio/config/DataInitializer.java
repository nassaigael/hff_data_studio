package com.henri_fraise.hff_data_studio.config;

import com.henri_fraise.hff_data_studio.entity.Permission;
import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import com.henri_fraise.hff_data_studio.enums.UserRole;
import com.henri_fraise.hff_data_studio.repository.PermissionRepository;
import com.henri_fraise.hff_data_studio.repository.UserCategoryRepository;
import com.henri_fraise.hff_data_studio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final UserCategoryRepository categoryRepository;
	private final PermissionRepository permissionRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	@Override
	public void run(String @NonNull ... args) {
		initializePermissions();
		initializeAdminUser();
	}

	private void initializePermissions() {
		if (permissionRepository.count() == 0) {
			log.info("📝 Initializing default permissions...");

			List<Permission> permissions = List.of(
					createPermission("FILE_UPLOAD", "Upload de fichiers", "Import"),
					createPermission("FILE_VIEW", "Visualisation des fichiers", "Import"),
					createPermission("FILE_DELETE", "Suppression de fichiers", "Import"),

					// Cleaning Module
					createPermission("CLEANING_RULE_CREATE", "Création de règles de nettoyage", "Cleaning"),
					createPermission("CLEANING_RULE_UPDATE", "Modification de règles de nettoyage", "Cleaning"),
					createPermission("CLEANING_RULE_DELETE", "Suppression de règles de nettoyage", "Cleaning"),
					createPermission("CLEANING_EXECUTE", "Exécution du nettoyage", "Cleaning"),
					createPermission("CLEANING_VIEW", "Visualisation de l'historique", "Cleaning"),

					// Analysis Module
					createPermission("ANALYSIS_RUN", "Lancement d'analyses", "Analysis"),
					createPermission("ANALYSIS_VIEW", "Visualisation des résultats", "Analysis"),
					createPermission("ANALYSIS_EXPORT", "Export des résultats", "Analysis"),
					createPermission("ANALYSIS_MODEL_SAVE", "Sauvegarde de modèles d'analyse", "Analysis"),

					// Export Module
					createPermission("EXPORT_CSV", "Export au format CSV", "Export"),
					createPermission("EXPORT_EXCEL", "Export au format Excel", "Export"),
					createPermission("EXPORT_IMAGE", "Export au format image", "Export"),
					createPermission("EXPORT_ZIP", "Export au format ZIP", "Export"),

					// Admin Module
					createPermission("USER_VIEW", "Visualisation des utilisateurs", "Admin"),
					createPermission("USER_MANAGE", "Gestion des utilisateurs", "Admin"),
					createPermission("CATEGORY_VIEW", "Visualisation des catégories", "Admin"),
					createPermission("CATEGORY_MANAGE", "Gestion des catégories", "Admin"),
					createPermission("PERMISSION_VIEW", "Visualisation des permissions", "Admin"),
					createPermission("PERMISSION_MANAGE", "Gestion des permissions", "Admin"),
					createPermission("AUDIT_VIEW", "Visualisation des logs d'audit", "Admin")
			);

			permissionRepository.saveAll(permissions);
			log.info("{} permissions initialized", permissions.size());
		}
	}

	private void initializeAdminUser() {
		if (userRepository.count() == 0) {
			log.info("Creating first admin user...");

			UserCategory adminCategory = categoryRepository.findByLabel("ADMIN")
					.orElseGet(() -> {
						UserCategory newCategory = UserCategory.builder()
								.label("ADMIN")
								.description("Administrator - Full access")
								.accessLevel(5)
								.build();
						return categoryRepository.save(newCategory);
					});

			List<Permission> allPermissions = permissionRepository.findAll();
			adminCategory.setPermissions(allPermissions);
			categoryRepository.save(adminCategory);

			User adminUser = User.builder()
					.lastName("Administrator")
					.firstName("System")
					.email("admin@hff.re")
					.passwordHash(passwordEncoder.encode("123456"))
					.isActive(true)
					.role(UserRole.ADMIN)
					.category(adminCategory)
					.build();

			userRepository.save(adminUser);

			log.info("Admin user created successfully!");
			log.info("Email: admin@hff.re");
			log.info("Password: 123456");
			log.info("Role: {}", UserRole.ADMIN.getDisplayName());
			log.info("Permissions: {} permissions assigned", allPermissions.size());
		}
	}

	private Permission createPermission(String code, String label, String module) {
		return Permission.builder()
				.code(code)
				.label(label)
				.module(module)
				.isActive(true)
				.build();
	}
}