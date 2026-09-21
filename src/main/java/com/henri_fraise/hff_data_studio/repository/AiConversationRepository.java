package com.henri_fraise.hff_data_studio.repository;

import com.henri_fraise.hff_data_studio.entity.AiConversation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {

	List<AiConversation> findByUserIdOrderByUpdatedAtDesc(UUID userId);

	List<AiConversation> findByUserIdAndContextTypeAndContextId(UUID userId, String contextType, UUID contextId);
}