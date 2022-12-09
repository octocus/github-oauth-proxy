package io.github.octocus.oauthproxy.config;

public interface GithubOAuthConfig {

    String getClientId();

    String getClientSecret();

    String getAccessTokenUrl();
}
