package com.dev.luqman.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogResponse {
    private List<SearchResponse> results;
    private String nextPageToken;
    private List<String> unreachable;
}
