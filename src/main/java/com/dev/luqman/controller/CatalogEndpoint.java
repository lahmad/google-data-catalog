package com.dev.luqman.controller;

import com.dev.luqman.exception.InvalidParamException;
import com.dev.luqman.filters.bigquery.TypeFilter;
import com.dev.luqman.filters.bigquery.TypeSortAttribute;
import com.dev.luqman.model.CatalogResponse;
import com.dev.luqman.model.EntrySearchResponse;
import com.dev.luqman.model.TagResponse;
import com.dev.luqman.request.PaginatedRequest;
import com.dev.luqman.response.pagination.PaginationResponse;
import com.dev.luqman.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Objects;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogEndpoint {

    private final CatalogService catalogService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public PaginationResponse<CatalogResponse> searchCatalog(@RequestBody @Valid  PaginatedRequest<TypeFilter, TypeSortAttribute> body) throws InvalidParamException {
        validateRequest(body);
        return catalogService.searchCatalog(body);
    }

    @GetMapping("/{entry}/columns")
    public PaginationResponse<EntrySearchResponse> searchColumns(@PathVariable("entry") @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Only alphanumeric values") String entry) {
        return catalogService.searchEntry(entry);
    }

    @GetMapping("/{entry}/tags")
    public PaginationResponse<TagResponse> searchColumns(@Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Only alphanumeric values") @PathVariable("entry") String entry, @RequestParam("pageToken") String pageToken, @Min (value = 1, message = "Minimum value is 1")@RequestParam("pageSize") int pageSize) {
        return catalogService.searchTags(entry, pageToken, pageSize);
    }
    private void validateRequest(final PaginatedRequest<TypeFilter, TypeSortAttribute> body) {

        if (body.getFilter() == null) {
            throw new InvalidParamException("Filter is a mandatory field.");
        }
        Optional.ofNullable(body.getFilter()).ifPresent(typeFilter -> {
            if (Objects.isNull(typeFilter.getType())) {
                throw new InvalidParamException("Filter should have type.");
            }
            if (!StringUtils.hasLength(body.getFilter().getName())) {
                throw new InvalidParamException("Filter should have name.");
            }
        });
    }


}
