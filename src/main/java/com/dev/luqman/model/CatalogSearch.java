package com.dev.luqman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CatalogSearch {
    private Scope scope;
    private String query;
    private Integer pageSize;
    private String pageToken;
    private String orderBy;

}
