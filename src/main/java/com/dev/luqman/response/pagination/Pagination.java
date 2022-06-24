package com.dev.luqman.response.pagination;

import lombok.Data;

@Data
public class Pagination {
    private Integer offset;
    private Integer total;
    private Integer limit;

    public Pagination() {
        this.offset = 0;
        this.total = 0;
        this.limit = 25;
    }
}
