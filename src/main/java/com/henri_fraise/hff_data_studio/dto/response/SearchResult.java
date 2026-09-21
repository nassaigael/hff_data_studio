package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

	private String entityType;
	private String entityId;
	private String title;
	private String snippet;
	private Double rank;
	private List<String> highlights;
}