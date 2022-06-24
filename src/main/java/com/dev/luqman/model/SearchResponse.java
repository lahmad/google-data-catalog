package com.dev.luqman.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {
    private SearchResultType searchResultType;
    private String searchResultSubtype;
    private String relativeResourceName;
    private String linkedResource;
    private String modifyTime;
    private String fullyQualifiedName;
    private String displayName;
    private String description;
    private IntegeratedSystem integeratedSystem;
    private String userSpecifiedSystem;
}
