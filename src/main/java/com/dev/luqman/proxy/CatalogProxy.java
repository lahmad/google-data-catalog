package com.dev.luqman.proxy;

import com.dev.luqman.model.CatalogResponse;
import com.dev.luqman.model.EntrySearchResponse;
import com.dev.luqman.model.TagResponse;
import com.dev.luqman.response.pagination.Pagination;

import java.util.Optional;

public interface CatalogProxy {
    Optional<CatalogResponse> searchCatalog(String type, int pageSize, String orderBy, String pageToken, String name);

    Optional<EntrySearchResponse> searchEntry(String entry);

    Optional<TagResponse> getTagTemplates(String entry, int pageSize, String pageToken);
}
