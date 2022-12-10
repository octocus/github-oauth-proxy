package io.github.octocus.oauthproxy.config;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemConfigTest {

    static final String TEST_CLIENT_ID = "clientId";
    static final String TEST_CLIENT_SECRET = "clientSecret";

    @Test
    void githubClientIdIsRequired() {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("GITHUB_CLIENT_SECRET", TEST_CLIENT_SECRET);

        assertThrows(IllegalArgumentException.class, () -> {
            new SystemConfig(systemEnv);
        });
    }

    @Test
    void githubClientSecretIsRequired() {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("GITHUB_CLIENT_ID", TEST_CLIENT_ID);

        assertThrows(IllegalArgumentException.class, () -> {
            new SystemConfig(systemEnv);
        });
    }

    @Test
    void readFromSystemEnvWithDefaultValues() {
        Map<String, String> systemEnv = new HashMap<>();

        final String githubClientId = TEST_CLIENT_ID;
        systemEnv.put("GITHUB_CLIENT_ID", githubClientId);

        final String githubClientSecret = TEST_CLIENT_SECRET;
        systemEnv.put("GITHUB_CLIENT_SECRET", githubClientSecret);

        systemEnv.put("OTHER_VAR", "VALUE");

        SystemConfig systemConfig = new SystemConfig(systemEnv);
        assertEquals(githubClientId, systemConfig.getClientId());
        assertEquals(githubClientSecret, systemConfig.getClientSecret());
        assertEquals("https://github.com/login/oauth/access_token", systemConfig.getAccessTokenUrl());
        assertEquals(8080, systemConfig.getHttpServerPort());
        assertEquals("/access_token", systemConfig.getAccessTokenPath());
    }

    @Test
    void customizeAccessTokenUrl() {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("GITHUB_CLIENT_ID", TEST_CLIENT_ID);
        systemEnv.put("GITHUB_CLIENT_SECRET", TEST_CLIENT_SECRET);
        final String githubAccessTokenUrl = "http://localhost:8081/path";
        systemEnv.put("GITHUB_ACCESS_TOKEN_URL", githubAccessTokenUrl);

        SystemConfig systemConfig = new SystemConfig(systemEnv);
        assertEquals(githubAccessTokenUrl, systemConfig.getAccessTokenUrl());
    }

    @Test
    void customizeHttpServerPort() {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("GITHUB_CLIENT_ID", TEST_CLIENT_ID);
        systemEnv.put("GITHUB_CLIENT_SECRET", TEST_CLIENT_SECRET);
        final int httpPort = 9090;
        systemEnv.put("HTTP_PORT", Integer.toString(httpPort));

        SystemConfig systemConfig = new SystemConfig(systemEnv);
        assertEquals(httpPort, systemConfig.getHttpServerPort());
    }

    @Test
    void customizeAccessTokenPath() {
        Map<String, String> systemEnv = new HashMap<>();
        systemEnv.put("GITHUB_CLIENT_ID", TEST_CLIENT_ID);
        systemEnv.put("GITHUB_CLIENT_SECRET", TEST_CLIENT_SECRET);
        final String accessTokenPath = "/foobar";
        systemEnv.put("ACCESS_TOKEN_PATH", accessTokenPath);

        SystemConfig systemConfig = new SystemConfig(systemEnv);
        assertEquals(accessTokenPath, systemConfig.getAccessTokenPath());
    }
}
