package com.henri_fraise.hff_data_studio.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
	private UUID favoriteId;
	private String entityType;
	private UUID entityId;
	private String displayName;
	private LocalDateTime createdAt;
}