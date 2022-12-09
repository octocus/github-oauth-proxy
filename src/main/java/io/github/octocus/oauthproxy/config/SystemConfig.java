package io.github.octocus.oauthproxy.config;

import java.util.Map;
import java.util.Optional;

public class SystemConfig implements OAuthProxyConfiguration {

    private static final int DEFAULT_HTTP_SERVER_PORT = 8080;
    private static final String DEFAULT_ACCESS_TOKEN_PATH = "/access_token";
    private static final String DEFAULT_GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";

    private final String clientId;
    private final String clientSecret;
    private final int httpServerPort;
    private final String accessTokenPath;
    private final String accessTokenUrl;

    public SystemConfig(Map<String, String> systemEnv) {
        this.clientId = systemEnv.get("GITHUB_CLIENT_ID");
        if (this.clientId == null) {
            throw new IllegalArgumentException("Missed the required environment variable: GITHUB_CLIENT_ID");
        }
        this.clientSecret = systemEnv.get("GITHUB_CLIENT_SECRET");
        if (this.clientSecret == null) {
            throw new IllegalArgumentException("Missed the required environment variable: GITHUB_CLIENT_SECRET");
        }
        this.accessTokenUrl = Optional.ofNullable(systemEnv.get("GITHUB_ACCESS_TOKEN_URL"))
                .orElse(DEFAULT_GITHUB_ACCESS_TOKEN_URL);
        this.httpServerPort = Optional.ofNullable(systemEnv.get("HTTP_PORT"))
                .map(Integer::valueOf)
                .orElse(DEFAULT_HTTP_SERVER_PORT);
        this.accessTokenPath = Optional.ofNullable(systemEnv.get("ACCESS_TOKEN_PATH"))
                .orElse(DEFAULT_ACCESS_TOKEN_PATH);
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    @Override
    public int getHttpServerPort() {
        return httpServerPort;
    }

    @Override
    public String getAccessTokenPath() {
        return accessTokenPath;
    }
}
