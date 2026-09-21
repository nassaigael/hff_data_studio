package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.AiRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "ai_message")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "message_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conversation_id", nullable = false)
	private AiConversation conversation;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private AiRole role;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "tokens_used")
	private Integer tokensUsed;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}