package com.henri_fraise.hff_data_studio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "glossary_term")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GlossaryTerm {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "term_id")
	private UUID id;

	@Column(name = "term", nullable = false, unique = true, length = 200)
	private String term;

	@Column(name = "definition", nullable = false, columnDefinition = "TEXT")
	private String definition;

	@Column(name = "synonyms", columnDefinition = "TEXT")
	private String synonyms;

	@Column(name = "related_terms", columnDefinition = "TEXT")
	private String relatedTerms;

	@Column(name = "domain", length = 100)
	private String domain;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private User createdBy;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}