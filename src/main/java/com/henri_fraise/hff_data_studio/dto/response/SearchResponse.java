package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
	private String query;
	private long totalHits;
	private List<SearchResult> results;
	private long tookMs;
}