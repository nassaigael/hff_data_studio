package com.henri_fraise.hff_data_studio.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
  private List<T> content;
  private Integer pageNumber;
  private Integer pageSize;
  private Long totalElements;
  private Integer totalPages;
  private Boolean isFirst;
  private Boolean isLast;
  private Boolean hasNext;
  private Boolean hasPrevious;

  public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
    return PageResponse.<T>builder()
        .content(page.getContent())
        .pageNumber(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .isFirst(page.isFirst())
        .isLast(page.isLast())
        .hasNext(page.hasNext())
        .hasPrevious(page.hasPrevious())
        .build();
  }
}
