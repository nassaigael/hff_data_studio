package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatasetRowsResponse {
	private List<String> columns;
	private List<Map<String, Object>> rows;
	private long totalRows;
	private int pageNumber;
	private int pageSize;
	private int totalPages;
	private boolean hasNext;
	private boolean hasPrevious;
}