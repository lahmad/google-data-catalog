package com.dev.luqman.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CatalogRequest {
    @NotEmpty(message = "type cannot be empty or null")
    private String type;

    @NotEmpty(message = "search criteria cannot be empty or null")
    private String search;
}
