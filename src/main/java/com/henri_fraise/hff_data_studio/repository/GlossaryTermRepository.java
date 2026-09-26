package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.GlossaryTerm;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlossaryTermRepository extends JpaRepository<GlossaryTerm, UUID> {

	Optional<GlossaryTerm> findByTermIgnoreCase(String term);

	List<GlossaryTerm> findByDomain(String domain);

	List<GlossaryTerm> findByTermContainingIgnoreCase(String keyword);

	boolean existsByTermIgnoreCase(String term);
}