package com.dev.luqman.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Column {
    private String type;
    private String description;
    private String mode;
    private String column;
}
