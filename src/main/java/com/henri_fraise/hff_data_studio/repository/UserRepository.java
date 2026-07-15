package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);
	Optional<User> findByEmailIsAndIsActiveTrue(String email);
	Page<User> findAllByIsActiveTrue(Pageable pageable);
	List<User> findAllByIsActiveTrue();
	Page<User> findByIsActiveTrueAndLastNameContainingIgnoreCaseOrFirstNameContainingIgnoreCase(String lastName, String firstName, Pageable pageable);
	Page<User> findByCategory_Id(UUID categoryId, Pageable pageable);
	List<User> findByLastLoginBefore(LocalDateTime date);

	boolean existsByEmail(String email);
	boolean existsByEmailAndIsActiveTrue(String email);
	boolean existsByEmailAndIdNot(String email, UUID id);

	long countByIsActiveTrue();
	long countByCategory_Id(UUID categoryId);

}
