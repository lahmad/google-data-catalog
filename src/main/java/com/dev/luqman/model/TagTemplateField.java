package com.dev.luqman.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Map;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagTemplateField {
    private String name;
    private String displayName;
    private Boolean isPubliclyReadable;
    private Map<String ,Object> fields;
}
