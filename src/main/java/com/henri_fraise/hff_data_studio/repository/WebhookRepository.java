package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.Webhook;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, UUID> {

	List<Webhook> findByUserIdOrderByCreatedAtDesc(UUID userId);

	List<Webhook> findByIsActiveTrue();
}