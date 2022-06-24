package com.dev.luqman.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Scope {
    private List<String> includeOrgIds;
    private List<String> includeProjectIds;
    private Boolean includeGcpPublicDatasets;
    private List<String> restrictedLocations;
    private Boolean starredOnly;
    private Boolean includePublicTagTemplates;

}
