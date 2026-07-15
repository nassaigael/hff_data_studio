package com.henri_fraise.hff_data_studio.mapper;

import com.henri_fraise.hff_data_studio.dto.response.PageResponse;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

  public <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
    if (page == null)
      return PageResponse.<R>builder()
          .content(List.of())
          .pageNumber(0)
          .pageSize(20)
          .totalElements(0L)
          .totalPages(0)
          .isFirst(true)
          .isLast(true)
          .hasNext(false)
          .hasPrevious(false)
          .build();

    List<R> content = page.getContent().stream().map(mapper).toList();

    return PageResponse.<R>builder()
        .content(content)
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

  public <T, R> PageResponse<R> toPageResponse(
      List<T> content, Page<T> page, Function<T, R> mapper) {
    if (page == null || content == null)
      return PageResponse.<R>builder()
          .content(List.of())
          .pageNumber(0)
          .pageSize(20)
          .totalElements(0L)
          .totalPages(0)
          .isFirst(true)
          .isLast(true)
          .hasNext(false)
          .hasPrevious(false)
          .build();

    List<R> mappedContent = content.stream().map(mapper).toList();

    return PageResponse.<R>builder()
        .content(mappedContent)
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
