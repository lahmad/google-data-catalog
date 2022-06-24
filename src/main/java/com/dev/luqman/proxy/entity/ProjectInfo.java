package com.dev.luqman.proxy.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectInfo {
    private String relativeResourceName;
    private String linkedResource;
    private String template;
}
