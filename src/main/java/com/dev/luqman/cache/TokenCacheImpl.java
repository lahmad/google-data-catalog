package com.dev.luqman.cache;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TokenCacheImpl implements TokenCache{
    private static final int MAX_SIZE = 1;
    private static final int CACHE_TTL_IN_MINS = 60;
    private static final String TOKEN_KEY = "TOKEN_KEY";
    private static final String CLOUD_PLATFORM = "https://www.googleapis.com/auth/cloud-platform";

    private LoadingCache<String, AccessToken> tokenCache;

    @Override
    public String getAccessToken() throws IOException {
        String token = null;
        try {
            final AccessToken accessToken =  tokenCache.get(TOKEN_KEY);
            log.info("{}", accessToken);
            return accessToken.getTokenValue();
        } catch (Exception ex) {
            token = getAccessTokenFromGoogle().getTokenValue();
        }
        return token;
    }

    @Override
    public GoogleCredentials getGoogleCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(ResourceUtils.getFile("classpath:service.json")))
                .createScoped(Arrays.asList(CLOUD_PLATFORM));
    }


    @PostConstruct
    private void initializeCache() {
        this.tokenCache = CacheBuilder.newBuilder()
                .initialCapacity(MAX_SIZE)
                .maximumSize(MAX_SIZE)
                .refreshAfterWrite(CACHE_TTL_IN_MINS, TimeUnit.MINUTES)
                .build(new CacheLoader<String, AccessToken>() {
                    @Override
                    public AccessToken load(String s) throws Exception {
                        final AccessToken token =  getAccessTokenFromGoogle();
                        log.info("Received the token {}", token);
                        return token;
                    }
                });
        try {
            tokenCache.put(TOKEN_KEY, getAccessTokenFromGoogle());
        } catch (Exception ex) {
            log.warn("Failed to get access token");
        }
    }

    private AccessToken getAccessTokenFromGoogle() throws IOException {
       return getGoogleCredentials().refreshAccessToken();
    }
}

