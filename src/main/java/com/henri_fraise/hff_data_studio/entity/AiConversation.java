package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "ai_conversation")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiConversation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "conversation_id")
	private UUID id;

	@Column(name = "title", length = 200)
	private String title;

	@Column(name = "context_type", length = 50)
	private String contextType;

	@Column(name = "context_id")
	private UUID contextId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt ASC")
	private List<AiMessage> messages;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}