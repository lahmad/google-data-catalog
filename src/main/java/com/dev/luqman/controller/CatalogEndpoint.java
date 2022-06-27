package com.dev.luqman.controller;

import com.dev.luqman.exception.InvalidParamException;
import com.dev.luqman.model.ColumnDataResponse;
import com.dev.luqman.model.TableSearchResponse;
import com.dev.luqman.request.CatalogRequest;
import com.dev.luqman.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

@Validated
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogEndpoint {

    private final CatalogService catalogService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public TableSearchResponse searchCatalog(@RequestBody @Valid  CatalogRequest body) throws InvalidParamException {
        return catalogService.searchCatalog(body);
    }

    @GetMapping("/{entry}/columns")
    public ColumnDataResponse ColumnDataResponsesearchColumns(@PathVariable("entry") @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Only alphanumeric values") String entry) {
        return catalogService.searchEntry(entry);
    }
}
