package com.dev.luqman.filters.bigquery;

import com.dev.luqman.request.search.BaseFilter;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TypeFilter extends BaseFilter {

    @NotNull(message = "Type cannot be null")
    private CatalogTypes type;

    private String name;

    private String column;
}
