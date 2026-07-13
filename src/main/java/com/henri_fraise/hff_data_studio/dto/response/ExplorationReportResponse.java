package com.henri_fraise.hff_data_studio.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExplorationReportResponse {
    private UUID reportId;
    private LocalDateTime generatedAt;
    private Integer totalRows;
    private Integer duplicateCount;
    private Integer missingValueCount;
    private BigDecimal qualityScore;
    private String reportPdfPath;
    private UUID datasetId;
    private Object columnStatistics;
}
