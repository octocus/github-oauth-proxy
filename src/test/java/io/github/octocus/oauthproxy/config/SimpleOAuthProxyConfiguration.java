package io.github.octocus.oauthproxy.config;

public class SimpleOAuthProxyConfiguration implements OAuthProxyConfiguration {

    private final String clientId;
    private final String clientSecret;
    private final String accessTokenUrl;
    private final int httpServerPort;
    private final String accessTokenPath;

    public SimpleOAuthProxyConfiguration(
            String clientId,
            String clientSecret,
            String accessTokenUrl,
            int httpServerPort,
            String accessTokenPath) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenUrl = accessTokenUrl;
        this.httpServerPort = httpServerPort;
        this.accessTokenPath = accessTokenPath;
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