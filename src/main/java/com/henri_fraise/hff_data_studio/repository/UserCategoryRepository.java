package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.User;
import com.henri_fraise.hff_data_studio.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCategoryRepository extends JpaRepository<UserCategory, UUID> {
	Optional<UserCategory> findByLabel(String label);
	List<UserCategory> findAllByOrderByAccessLevelDesc();
	List<UserCategory> findByAccessLevelGreaterThanEqual(Integer accessLevel);

	boolean existsByLabel(String label);
	boolean existsByLabelAndIdNot(String label, UUID id);

	@Query("SELECT c FROM UserCategory c JOIN c.users u WHERE u.isActive = true GROUP BY c ORDER BY COUNT(u) DESC")
	List<UserCategory> findCategoriesWithMostActiveUsers();

	@Query("SELECT c FROM UserCategory  c WHERE  c.accessLevel <= :maxAccessLevel")
	List<UserCategory> findByMaxAccessLevel(@Param("maxAccessLevel") Integer maxAccessLevel);


}
