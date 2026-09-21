package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

	List<Tag> findByUserIdOrderByNameAsc(UUID userId);

	Optional<Tag> findByUserIdAndName(UUID userId, String name);

	boolean existsByUserIdAndName(UUID userId, String name);
}