package com.henri_fraise.hff_data_studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlossaryTermRequest {

	@NotBlank private String term;
	@NotBlank private String definition;
	private List<String> synonyms;
	private List<String> relatedTerms;
	private String domain;
}