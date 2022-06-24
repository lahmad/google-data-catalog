package com.dev.luqman.filters.bigquery;

import com.dev.luqman.request.sorting.SortAttribute;

public enum TypeSortAttribute implements SortAttribute {
    modifyTime;

    @Override
    public String getName() {
        return this.name();
    }
}
