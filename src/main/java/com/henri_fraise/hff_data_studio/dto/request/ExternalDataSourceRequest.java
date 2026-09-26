package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.DataSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDataSourceRequest {

	@NotBlank(message = "Name is required")
	private String name;

	@NotNull(message = "Type is required")
	private DataSourceType type;

	@NotNull(message = "Configuration is required")
	private Map<String, Object> config;

	private Map<String, Object> credentials;
}