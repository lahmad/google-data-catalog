package com.dev.luqman.request;

import com.dev.luqman.request.search.BaseFilter;
import com.dev.luqman.request.sorting.SortAttribute;
import com.dev.luqman.request.sorting.Sorting;
import com.dev.luqman.response.pagination.Pagination;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaginatedRequest<F extends BaseFilter, A extends SortAttribute> {
    private Pagination pagination;
    private F filter;
    private Sorting<A> sorting;
}
