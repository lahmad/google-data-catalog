package com.dev.luqman.response.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class PaginationResponse<R> {
    private List<R> data;
    private Pagination pagination;
}
