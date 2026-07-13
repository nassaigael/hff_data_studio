package com.henri_fraise.hff_data_studio.dto.request;

import com.henri_fraise.hff_data_studio.enums.ExportFormat;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExportRequest {

    @NotNull(message = "Execution ID is required")
    private UUID executionId;

    @NotNull(message = "Export format is required")
    private ExportFormat exportFormat;

    private List<UUID> resultIds;
}
