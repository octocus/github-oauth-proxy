package io.github.octocus.oauthproxy;

import io.github.octocus.oauthproxy.config.SimpleOAuthProxyConfiguration;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith({
        VertxExtension.class,
})
class OAuthProxyVerticleTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String MOCK_ACCESS_TOKEN_URL = "/foobar";

    MockWebServer mockWebServer;
    SimpleOAuthProxyConfiguration configuration;
    String deploymentId;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        mockWebServer = new MockWebServer();
        String mockServerUrl = String.format("http://%s:%d", mockWebServer.getHostName(), mockWebServer.getPort());
        configuration = new SimpleOAuthProxyConfiguration(
                "test-client-id",
                "test-client-secret",
                mockServerUrl + MOCK_ACCESS_TOKEN_URL,
                8081,
                "/access_token"
        );
        vertx.deployVerticle(new OAuthProxyVerticle(configuration))
                .onSuccess(deploymentId -> {
                    this.deploymentId = deploymentId;
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @AfterEach
    void tearDown(Vertx vertx, VertxTestContext testContext) throws IOException {
        mockWebServer.close();
        vertx.undeploy(deploymentId).onComplete(ar -> testContext.completeNow());
    }

    @Test
    void unknownPathShouldReturnNotFound(Vertx vertx, VertxTestContext testContext) {
        vertx.createHttpClient()
                .request(
                        HttpMethod.GET,
                        configuration.getHttpServerPort(),
                        LOCALHOST,
                        "/foobar")
                .compose(HttpClientRequest::send)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(404, response.statusCode());
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }

    @Test
    void optionsRequest(Vertx vertx, VertxTestContext testContext) {
        vertx.createHttpClient()
                .request(
                        HttpMethod.OPTIONS,
                        configuration.getHttpServerPort(),
                        LOCALHOST,
                        configuration.getAccessTokenPath())
                .compose(HttpClientRequest::send)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(200, response.statusCode());
                    MultiMap headers = response.headers();
                    assertEquals("*", headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                    assertEquals("POST, OPTIONS", headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }

    @Test
    void proxyAccessTokenRequest(Vertx vertx, VertxTestContext testContext) {
        final String responsePayloadReceived = "{\"error\":\"bad_verification_code\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(responsePayloadReceived)
                .setResponseCode(200)
                .setHeader("content-type", "application/json"));
        vertx.createHttpClient()
                .request(
                        HttpMethod.POST,
                        configuration.getHttpServerPort(),
                        LOCALHOST,
                        configuration.getAccessTokenPath())
                .compose(request -> {
                    request.putHeader("accept", "application/json");
                    return request.send();
                })
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(200, response.statusCode());
                    MultiMap headers = response.headers();
                    assertEquals("*", headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
                    assertEquals("POST, OPTIONS", headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
                    assertEquals("application/json", headers.get(HttpHeaders.CONTENT_TYPE));

                    response.body().onComplete(body -> {
                        String actualResponse = body.result().toString();
                        assertEquals(responsePayloadReceived, actualResponse);
                        testContext.completeNow();
                    });
                }))
                .onFailure(testContext::failNow);

        final RecordedRequest recordedRequest;
        try {
            recordedRequest = mockWebServer.takeRequest();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Failed to take the request from the mock server because of an interruption.");
            return;
        }

        HttpUrl requestUrl = recordedRequest.getRequestUrl();
        assertNotNull(requestUrl);
        assertEquals(MOCK_ACCESS_TOKEN_URL, requestUrl.encodedPath());
        assertEquals(configuration.getClientId(), requestUrl.queryParameter("client_id"));
        assertEquals(configuration.getClientSecret(), requestUrl.queryParameter("client_secret"));
    }

    @Test
    void testWhenGithubResponsesErrorWithoutPayload(Vertx vertx, VertxTestContext testContext) {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        vertx.createHttpClient()
                .request(
                        HttpMethod.POST,
                        configuration.getHttpServerPort(),
                        LOCALHOST,
                        configuration.getAccessTokenPath())
                .compose(HttpClientRequest::send)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(500, response.statusCode());
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }

    @Test
    void testWhenGithubDoesNotResponse(Vertx vertx, VertxTestContext testContext) throws Exception {
        mockWebServer.close();
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        vertx.createHttpClient()
                .request(
                        HttpMethod.POST,
                        configuration.getHttpServerPort(),
                        LOCALHOST,
                        configuration.getAccessTokenPath())
                .compose(HttpClientRequest::send)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(500, response.statusCode());
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }

    @Test
    void getMethodIsNotSupported(Vertx vertx, VertxTestContext testContext) {
        vertx.createHttpClient()
                .request(
                        HttpMethod.GET,
                        configuration.getHttpServerPort(),
                        LOCALHOST,
                        configuration.getAccessTokenPath())
                .compose(HttpClientRequest::send)
                .onSuccess(response -> testContext.verify(() -> {
                    assertEquals(405, response.statusCode());
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }
}
