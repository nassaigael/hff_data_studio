package com.henri_fraise.hff_data_studio.dto.response;

import com.henri_fraise.hff_data_studio.enums.DataSourceType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDataSourceResponse {
	private UUID sourceId;
	private String name;
	private DataSourceType type;
	private Map<String, Object> config;
	private Boolean isActive;
	private LocalDateTime lastTestAt;
	private Boolean lastTestSuccess;
	private String lastError;
	private LocalDateTime createdAt;
}