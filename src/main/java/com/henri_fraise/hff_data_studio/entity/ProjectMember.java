package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.MemberRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "project_member",
		uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectMember {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "member_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	@Builder.Default
	private MemberRole role = MemberRole.VIEWER;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invited_by")
	private User invitedBy;

	@CreationTimestamp
	@Column(name = "joined_at", nullable = false, updatable = false)
	private LocalDateTime joinedAt;
}