package com.dev.luqman.model;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagResponse {
    private List<Tag> tags;

    private class Tag {
        private String name;
        private String template;
        private String templateDisplayName;
        private Map<String, Object> fields;
    }
}
