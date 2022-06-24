package com.dev.luqman.response.pagination;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedResult<T> {
    private List<T> entities;
    private int rowCount;
}
