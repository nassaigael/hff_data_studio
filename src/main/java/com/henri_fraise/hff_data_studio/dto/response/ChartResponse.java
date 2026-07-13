package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ChartType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartResponse {
	private UUID chartId;
	private ChartType chartType;
	private String chartTypeLabel;
	private Map<String, Object> configJson;
	private UUID resultId;
}
