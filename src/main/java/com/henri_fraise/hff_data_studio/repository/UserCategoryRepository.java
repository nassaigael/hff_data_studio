package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCategoryRepository extends JpaRepository<UserCategory, UUID> {
	Optional<UserCategory> findByLabel(String label);
	List<UserCategory> findAllByOrderByAccessLevelDesc();
	List<UserCategory> findByAccessLevelGreaterThanEqual(Integer accessLevel);
}
