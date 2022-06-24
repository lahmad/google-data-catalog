package com.dev.luqman.proxy;

import com.dev.luqman.cache.TokenCache;
import com.dev.luqman.model.*;
import com.dev.luqman.proxy.entity.ProjectInfo;
import com.dev.luqman.response.pagination.Pagination;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.api.client.util.Value;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.cache.CacheBuilder;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    public static final int INITIAL_CAPACITY = 100;

    @Value("#{'${google.catalog.projectids}'.split(',')}")
    private String[] projectIds;

    @Value("${connection.timeout.ms}")
    private int connectionTimeOutsInMs;

    @Value("${project.cache.max:500}")
    private int projectCacheMax;


    private final TokenCache tokenCache;
    private WebClient webClient;

    private Cache<String, List<ProjectInfo>> projectInfoCache;

    @Override
    public Optional<CatalogResponse> searchCatalog(String type, int pageSize, String orderBy, String pageToken, String name) {
        Optional<CatalogResponse> responseOptional = Optional.empty();
        try {
            final GoogleCredentials googleCredentials = tokenCache.getGoogleCredentials();
            Scope scope = Scope.builder().
                    includeProjectIds(Arrays.asList(projectIds)).
                    build();
            CatalogSearch search = CatalogSearch.builder()
                    .query(TYPE_TABLE)
                    .scope(scope)
                    .build();

            final CatalogResponse catalogResponse = webClient.post().uri(CATALOG_SEARCH)
                    .header(AUTHORIZATION, String.format(BEARER_S, tokenCache.getAccessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(search))
                    .retrieve()
                    .bodyToMono(CatalogResponse.class)
                    .block();

            if (Objects.nonNull(catalogResponse) && !CollectionUtils.isEmpty(catalogResponse.getResults())) {
                log.info("Received data back  from Google Data Catalog API.");

                if (projectInfoCache.getIfPresent(name) == null) {
                    List<ProjectInfo> projectInfos = catalogResponse.getResults().stream().map(searchResponse ->  {
                        return ProjectInfo.builder().linkedResource(searchResponse.getLinkedResource()).relativeResourceName(searchResponse.getRelativeResourceName()).build();
                    }).collect(Collectors.toList());

                    projectInfoCache.put(name, projectInfos);
                }

                return Optional.of(catalogResponse);
            }
        } catch (Exception ex) {
            log.error("Failed to receive response from Google: ex", ex);
        }
        return responseOptional;
    }

    @Override
    public Optional<EntrySearchResponse> searchEntry(String entry) {
        if (projectInfoCache.getIfPresent(entry) != null) {
            List<ProjectInfo> projectInfos = projectInfoCache.getIfPresent(entry);
            Optional<ProjectInfo> projectInfoOptional = projectInfos.stream().filter(p -> p.getLinkedResource().endsWith(entry)).findAny();
            if (projectInfoOptional.isPresent()) {

                try {
                    final EntrySearchResponse entrySearchResponse = webClient.get().uri(uriBuilder ->
                                    uriBuilder.path(ENTRIES_LOOKUP)
                                            .queryParam(projectInfoOptional.get().getLinkedResource()).build())
                            .header(AUTHORIZATION, String.format(BEARER_S, tokenCache.getAccessToken()))
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(EntrySearchResponse.class)
                            .block();

                    if (Objects.nonNull(entrySearchResponse)) {
                        log.info("Received entry info {}", entrySearchResponse);
                        return Optional.of(entrySearchResponse);
                    }
                } catch (Exception ex) {
                    log.error("Failed to get entry from Google Data Catalog Api. ex", ex);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<TagResponse> getTagTemplates(String entry, int pageSize, String pageToken) {

        if (projectInfoCache.getIfPresent(entry) != null) {
            List<ProjectInfo> projectInfos = projectInfoCache.getIfPresent(entry);
            Optional<ProjectInfo> projectInfoOptional = projectInfos.stream().filter(p -> p.getLinkedResource().endsWith(entry)).findAny();
            if (projectInfoOptional.isPresent()) {
                try {
                    final TagResponse tagResponse  = webClient.get().uri(uBuilder -> uBuilder.path(String.format("%s/tags", projectInfoOptional.get().getRelativeResourceName()))
                            .queryParam("pageSize", pageSize).queryParam("pageToken", pageToken).build())
                            .header(AUTHORIZATION, String.format(BEARER_S, tokenCache.getAccessToken()))
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(TagResponse.class)
                            .block();

                    if (Objects.nonNull(tagResponse)) {
                        log.info("Tag responses received {}", tagResponse);
                        return Optional.of(tagResponse);
                    }
                }
                catch (Exception ex) {
                    log.error("Failed to read tags in Google Data Catalog Api. ex", ex);
                }
            }
        } else {
            log.warn("The cache doesn't have the entry");
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
}
