package com.dev.luqman.service;

import com.dev.luqman.model.*;
import com.dev.luqman.proxy.CatalogProxy;
import com.dev.luqman.request.CatalogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogProxy catalogProxy;

    public TableSearchResponse searchCatalog(CatalogRequest request) {


        Optional<TableSearchResponse> catalogResponseOpt = catalogProxy.searchCatalog(request.getType(), request.getSearch());
        if (!catalogResponseOpt.isPresent()) {
            log.warn("Could not find the type={} with name = {}", request.getType(), request.getSearch());
            return  new TableSearchResponse(new HashSet<>());
        }

        log.info("Received result {}", catalogResponseOpt.get());
        return catalogResponseOpt.get();
    }

    public ColumnDataResponse searchEntry(final String entry) {
        Optional<ColumnDataResponse> entrySearchResponseOpt = catalogProxy.searchEntry(entry);
        if (!entrySearchResponseOpt.isPresent()) {
            log.warn("Could not find the type={} with name = {}", entry);
            return  ColumnDataResponse.builder().build();
        }
        log.info("Received result {}", entrySearchResponseOpt.get());
        return  entrySearchResponseOpt.get();
    }
}
