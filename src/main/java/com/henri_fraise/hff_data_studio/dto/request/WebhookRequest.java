package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.WebhookProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {

	@NotBlank(message = "Name is required")
	private String name;

	@NotBlank(message = "URL is required")
	private String url;

	@NotNull(message = "Provider is required")
	private WebhookProvider provider;

	@NotNull(message = "Event types are required")
	private List<String> eventTypes;

	private String secret;

	private Boolean isActive;
}