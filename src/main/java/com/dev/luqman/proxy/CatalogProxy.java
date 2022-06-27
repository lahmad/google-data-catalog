package com.dev.luqman.proxy;

import com.dev.luqman.model.*;
import com.dev.luqman.response.pagination.Pagination;

import java.util.Optional;

public interface CatalogProxy {
    Optional<TableSearchResponse> searchCatalog(String type, String name);

    Optional<ColumnDataResponse> searchEntry(String entry);

}
