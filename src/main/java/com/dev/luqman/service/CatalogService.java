package com.dev.luqman.service;

import com.dev.luqman.filters.bigquery.TypeFilter;
import com.dev.luqman.filters.bigquery.TypeSortAttribute;
import com.dev.luqman.model.CatalogResponse;
import com.dev.luqman.model.EntrySearchResponse;
import com.dev.luqman.model.TagResponse;
import com.dev.luqman.proxy.CatalogProxy;
import com.dev.luqman.request.PaginatedRequest;
import com.dev.luqman.response.pagination.Pagination;
import com.dev.luqman.response.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogProxy catalogProxy;

    public PaginationResponse<CatalogResponse> searchCatalog(PaginatedRequest<TypeFilter, TypeSortAttribute> request) {

        final String type = request.getFilter().getType().getType();
        final String name = request.getFilter().getName();
        final int pageSize = request.getPagination().getOffset();
        final String orderBy = request.getSorting().getDirection().name();

        Optional<CatalogResponse> catalogResponseOpt = catalogProxy.searchCatalog(type, pageSize,orderBy, null, name );
        if (!catalogResponseOpt.isPresent()) {
            log.warn("Could not find the type={} with name = {}", type, name);
            return  new PaginationResponse<CatalogResponse>(new ArrayList<>(), new Pagination());
        }

        log.info("Received result {}", catalogResponseOpt.get());
        Pagination pagination = new Pagination();
        pagination.setLimit(request.getPagination().getLimit());
        pagination.setOffset(request.getPagination().getOffset());
        pagination.setTotal(catalogResponseOpt.get().getResults().size());
        return new PaginationResponse<>(Arrays.asList(catalogResponseOpt.get()), pagination);
    }

    public PaginationResponse<EntrySearchResponse> searchEntry(final String entry) {
        Optional<EntrySearchResponse> entrySearchResponseOpt = catalogProxy.searchEntry(entry);
        if (!entrySearchResponseOpt.isPresent()) {
            log.warn("Could not find the type={} with name = {}", entry);
            return  new PaginationResponse<EntrySearchResponse>(new ArrayList<>(), new Pagination());
        }
        log.info("Received result {}", entrySearchResponseOpt.get());
        return new PaginationResponse<>(Arrays.asList(entrySearchResponseOpt.get()), new Pagination());
    }

    public PaginationResponse<TagResponse> searchTags(final String entry, final String pageToken, int pageSize) {
      Optional<TagResponse> tagResponseOpt = catalogProxy.getTagTemplates(entry, pageSize, pageToken);
        if (!tagResponseOpt.isPresent()) {
            log.warn("Could not find the type={} with name = {}", entry);
            return  new PaginationResponse<TagResponse>(new ArrayList<>(), new Pagination());
        }
        log.info("Received result {}", tagResponseOpt.get());

        Pagination pagination = new Pagination();
        pagination.setLimit(pageSize);
        return new PaginationResponse<>(Arrays.asList(tagResponseOpt.get()), pagination);
    }
}
