package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.TagColor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "tag", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "tag_id")
	private UUID id;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "color", nullable = false, length = 20)
	@Builder.Default
	private TagColor color = TagColor.BLUE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}