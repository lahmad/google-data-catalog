package com.dev.luqman.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntrySearchResponse {
    private String name;
    private String type;
    private Schema schema;

}
