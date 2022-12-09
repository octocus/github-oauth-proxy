package io.github.octocus.oauthproxy;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class CorsHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        CorsHelper.putCorsHeaders(response);
        response.end();
    }
}
