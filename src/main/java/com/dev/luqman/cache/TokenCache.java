package com.dev.luqman.cache;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;

public interface TokenCache {
    String getAccessToken() throws IOException;
    GoogleCredentials getGoogleCredentials() throws IOException;
}
