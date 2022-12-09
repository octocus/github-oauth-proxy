package io.github.octocus.oauthproxy;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;

public class CorsHelper {

    private static final String ALLOW_ALL_ORIGIN = "*";

    public static HttpServerResponse putCorsHeaders(HttpServerResponse response) {
        response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOW_ALL_ORIGIN);
        response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, OPTIONS");
        response.putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.CONTENT_TYPE);
        return response;
    }
}
