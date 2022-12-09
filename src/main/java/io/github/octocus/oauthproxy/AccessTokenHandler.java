package io.github.octocus.oauthproxy;

import io.github.octocus.oauthproxy.config.GithubOAuthConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessTokenHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenHandler.class);

    private static final String[] FORWARDED_HTTP_REQUEST_HEADERS = {
            HttpHeaders.CONTENT_TYPE.toString(),
            HttpHeaders.ACCEPT.toString(),
            HttpHeaders.USER_AGENT.toString()
    };

    private final WebClient client;
    private final GithubOAuthConfig githubOAuthConfig;

    public AccessTokenHandler(WebClient client, GithubOAuthConfig githubOAuthConfig) {
        this.client = client;
        this.githubOAuthConfig = githubOAuthConfig;
    }

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerRequest inboundRequest = ctx.request().pause();

        HttpRequest<Buffer> githubRequest = client.postAbs(githubOAuthConfig.getAccessTokenUrl());

        MultiMap inboundRequestHeader = inboundRequest.headers();
        for (String forwardedHttpRequestHeader : FORWARDED_HTTP_REQUEST_HEADERS) {
            if (inboundRequestHeader.contains(forwardedHttpRequestHeader)) {
                githubRequest.putHeader(
                        forwardedHttpRequestHeader,
                        inboundRequestHeader.get(forwardedHttpRequestHeader));
            }
        }
        githubRequest.queryParams().addAll(inboundRequest.params())
                .add("client_id", githubOAuthConfig.getClientId())
                .add("client_secret", githubOAuthConfig.getClientSecret());
        githubRequest.sendStream(inboundRequest).onSuccess(githubResponse -> {
            HttpServerResponse response = ctx.response();
            CorsHelper.putCorsHeaders(response);
            response.setStatusCode(githubResponse.statusCode());
            response.setStatusMessage(githubResponse.statusMessage());
            response.headers().addAll(githubResponse.headers());
            response.end(githubResponse.body());
        }).onFailure(e -> {
            log.error("Github Server inboundRequest failed.", e);
            HttpServerResponse response = ctx.response();
            CorsHelper.putCorsHeaders(response)
                    .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .end();
        });
    }
}
