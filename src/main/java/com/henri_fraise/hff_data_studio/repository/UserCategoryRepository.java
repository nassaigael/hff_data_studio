package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.UserCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory, UUID> {
	Optional<UserCategory> findByLabel(String label);

	List<UserCategory> findAllByOrderByAccessLevelDesc();

	List<UserCategory> findByAccessLevelGreaterThanEqual(Integer accessLevel);

	boolean existsByLabel(String label);

	boolean existsByLabelAndIdNot(String label, UUID id);

	@Query(
			"SELECT c FROM UserCategory c JOIN c.users u WHERE u.isActive = true GROUP BY c ORDER BY"
					+ " COUNT(u) DESC")
	List<UserCategory> findCategoriesWithMostActiveUsers();

	@Query("SELECT c FROM UserCategory  c WHERE  c.accessLevel <= :maxAccessLevel")
	List<UserCategory> findByMaxAccessLevel(@Param("maxAccessLevel") Integer maxAccessLevel);

	@Modifying
	@Transactional
	@Query("UPDATE UserCategory  c SET c.accessLevel = :accessLevel WHERE c.id = :category_id")
	void updateAccessLevel(
			@Param("category_id") UUID categoryId, @Param("accessLevel") Integer accessLevel);

	List<UserCategory> findByAccessLevelGreaterThanEqual(int accessLevel);

	Optional<UserCategory> findFirstByOrderByAccessLevelAsc();

	@Query("SELECT c FROM UserCategory c LEFT JOIN FETCH c.permissions")
	List<UserCategory> findAllWithPermissions();

	@Query("SELECT c FROM UserCategory c WHERE SIZE(c.users) > 0")
	List<UserCategory> findCategoriesWithUsers();

	@Query("SELECT COUNT(u) FROM User u WHERE u.category.id = :categoryId")
	long countUsersByCategoryId(@Param("categoryId") UUID categoryId);

	@Query("SELECT COUNT(u) FROM User u WHERE u.category.label = :label")
	long countUsersByCategoryLabel(@Param("label") String label);

	@Query("SELECT c.label, COUNT(u) FROM UserCategory c LEFT JOIN c.users u GROUP BY c.label ORDER BY COUNT(u) DESC")
	List<Object[]> getCategoryStatistics();

	@Query("SELECT c FROM UserCategory c WHERE c.label IN ('ADMIN', 'DATA_ANALYST', 'CONSULTANT', 'INVITE')")
	List<UserCategory> findDefaultCategories();
}
