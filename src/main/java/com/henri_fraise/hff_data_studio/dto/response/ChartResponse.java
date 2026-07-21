package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.ChartType;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartResponse {

  private UUID chartId;

  private ChartType chartType;

  private String chartTypeLabel;

  private Map<String, Object> configJson;

  private UUID resultId;

  private String resultTitle;

  private String resultType;

  private String imageUrl;
}
