package com.dev.luqman.request.sorting;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Sorting<A extends SortAttribute> {
    private SortDirection direction;
    private A attribute;
}
