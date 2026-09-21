package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlossaryTermResponse {
	private UUID termId;
	private String term;
	private String definition;
	private List<String> synonyms;
	private List<String> relatedTerms;
	private String domain;
	private LocalDateTime createdAt;
}