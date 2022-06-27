package com.dev.luqman.proxy;

import com.dev.luqman.cache.TokenCache;
import com.dev.luqman.model.*;
import com.dev.luqman.proxy.entity.ProjectInfo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.api.client.util.Value;
import com.google.auth.oauth2.GoogleCredentials;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogProxyImpl implements  CatalogProxy {

    private static final String BASE_URL = "https://datacatalog.googleapis.com/v1";
    private static final String CATALOG_SEARCH = "/catalog:search";

    private static final String ENTRIES_LOOKUP = "/entries:lookup";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_S = "Bearer %s";
    private static final String TYPE_TABLE = "type=table";
    private static final int INITIAL_CAPACITY = 100;

    private static final int TABLE_SEARCH_SIZE = 250;
    public static final String LINKED_RESOURCE = "linkedResource";

    @Value("${google.projectid:data-catalog-353918}")
    private String projectIds;

    @Value("${connection.timeout.ms}")
    private int connectionTimeOutsInMs;

    @Value("${project.cache.max:500}")
    private int projectCacheMax;

    private final TokenCache tokenCache;
    private WebClient webClient;

    private Cache<String, ProjectInfo> projectInfoCache;

    private Cache<String, ColumnDataResponse> tableCache;

    @Override
    public Optional<TableSearchResponse> searchCatalog(String type, String query) {
        Optional<TableSearchResponse> responseOptional = Optional.empty();
        try {
            final GoogleCredentials googleCredentials = tokenCache.getGoogleCredentials();
            Scope scope = Scope.builder().
                    includeProjectIds(Arrays.asList("data-catalog-353918")).
                    build();
            final String queryString = String.format("type=%s AND (name:%s OR column=%s)", type, query, query);
            log.info("Querying Google Data Catalog {}", queryString);
            CatalogSearch search = CatalogSearch.builder()
                    .query(queryString)
                    .scope(scope)
                    .pageSize(TABLE_SEARCH_SIZE)
                    .build();

            final CatalogResponse catalogResponse = webClient.post().uri(CATALOG_SEARCH)
                    .header(AUTHORIZATION, String.format(BEARER_S, tokenCache.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(search))
                    .retrieve()
                    .bodyToMono(CatalogResponse.class)
                    .block();

            Set<String> tables = new HashSet<>();
            if (Objects.nonNull(catalogResponse) && !CollectionUtils.isEmpty(catalogResponse.getResults())) {
                log.info("Received data back  from Google Data Catalog API.");
                for (SearchResponse searchResponse : catalogResponse.getResults()) {
                    final String linkedResource = searchResponse.getLinkedResource();
                    final String tableName = linkedResource.substring(linkedResource.lastIndexOf('/') + 1);
                    log.info("Received table name {}", tableName);

                    tables.add(tableName);
                    if (projectInfoCache.getIfPresent(tableName) == null) {
                        ProjectInfo projectInfo = ProjectInfo.builder().linkedResource(searchResponse.getLinkedResource())
                                .relativeResourceName(searchResponse.getRelativeResourceName()).build();
                        projectInfoCache.put(tableName, projectInfo);

                        // Async Call
                        getEntryResponseAsync(searchResponse.getLinkedResource(), tableName);
                    }
                }

                return Optional.of(new TableSearchResponse(tables));
            }
        } catch (Exception ex) {
            log.error("Failed to receive response from Google: ex", ex);
        }
        return responseOptional;
    }

    @Override
    public Optional<ColumnDataResponse> searchEntry(String entry) {
        final ProjectInfo projectInfo = projectInfoCache.getIfPresent(entry);
        try {
            if (projectInfo != null) {
                final ColumnDataResponse response = tableCache.getIfPresent(entry);
                if (response == null) {
                    getEntryResponseAsync(projectInfo.getLinkedResource(), entry)
                            .get();
                    return Optional.of(tableCache.getIfPresent(entry));
                }

            } else {
                log.warn("Table doesn't exist in the cache {}", entry);
            }
        } catch (Exception ex) {
            log.error("Failed to get entry from Google Data Catalog Api. ex", ex);
        }
        return Optional.empty();
    }

    @PostConstruct
    private void initialize() {
        final HttpClient httpClient = HttpClient.create().wiretap(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeOutsInMs)
                .responseTimeout(Duration.ofMillis(connectionTimeOutsInMs))
                .doOnConnected(connection -> {
                   connection.addHandlerFirst(new ReadTimeoutHandler(connectionTimeOutsInMs, TimeUnit.MILLISECONDS)).
                   addHandlerLast(new WriteTimeoutHandler(connectionTimeOutsInMs, TimeUnit.MILLISECONDS));
                });
        this.webClient = WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl(BASE_URL).filter(logRequest())
                .filter(logResponse())
                .build();

        this.projectInfoCache = Caffeine.newBuilder()
                .initialCapacity(INITIAL_CAPACITY)
                .maximumSize(projectCacheMax)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .recordStats()
                .build();

        this.tableCache = Caffeine.newBuilder()
                .initialCapacity(INITIAL_CAPACITY)
                .maximumSize(projectCacheMax)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .recordStats()
                .build();
    }
    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientResponse);
        });
    }

    private CompletableFuture<Void> getEntryResponseAsync(final String linkedResource, final String tableName) {
        return CompletableFuture.supplyAsync(() -> {
               try {
                   log.info("Making a call to get columns  for {} and table:{}", linkedResource, tableName);
                   EntrySearchResponse entrySearchResponse =  webClient.get().uri(uriBuilder ->
                                   uriBuilder.path(ENTRIES_LOOKUP)
                                           .queryParam(LINKED_RESOURCE, linkedResource).build())
                           .header(AUTHORIZATION, String.format(BEARER_S, tokenCache.getAccessToken()))
                           .accept(MediaType.APPLICATION_JSON)
                           .retrieve()
                           .bodyToMono(EntrySearchResponse.class)
                           .block();
                   entrySearchResponse.setTableName(tableName);
                   log.info("Received the response :{}", entrySearchResponse);

                   return entrySearchResponse;
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
        }).thenApply(entry -> {
            try {
                final TagResponse tagResponse  = webClient.get().uri(uBuilder -> uBuilder.path(String.format("%s/tags", entry.getName()))
                                .queryParam("pageSize", 1).build())
                        .header(AUTHORIZATION, String.format(BEARER_S, tokenCache.getAccessToken()))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(TagResponse.class)
                        .block();

                // Persist in the cache.
                if (entry.getSchema()!= null && tagResponse != null) {
                    Optional<Map<String, Object>> fieldsOpt = Optional.of(tagResponse).map(TagResponse::getTags).map(tags -> tags.get(0)).map(TagResponse.Tag::getFields);

                    ColumnDataResponse columnDataResponse = ColumnDataResponse.builder().columns(entry.getSchema().getColumns()).build();
                    if (fieldsOpt.isPresent()) {
                        Map<String, Object> tagFields = fieldsOpt.get();
                        columnDataResponse.setTags(tagFields);
                    }
                    log.info("Persisting in the cache table:{} and column:{}", tableName, columnDataResponse);
                    tableCache.put(entry.getTableName(), columnDataResponse);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}
