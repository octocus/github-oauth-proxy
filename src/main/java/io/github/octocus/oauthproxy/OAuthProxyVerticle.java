package io.github.octocus.oauthproxy;

import io.github.octocus.oauthproxy.config.OAuthProxyConfiguration;
import io.github.octocus.oauthproxy.config.SystemConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthProxyVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(OAuthProxyVerticle.class);

    private final OAuthProxyConfiguration configuration;

    public OAuthProxyVerticle(OAuthProxyConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        String accessTokenPath = configuration.getAccessTokenPath();
        router.options(accessTokenPath).handler(new CorsHandler());

        WebClient webClient = WebClient.create(vertx);
        router.post(accessTokenPath).handler(new AccessTokenHandler(webClient, configuration));

        int httpServerPort = configuration.getHttpServerPort();
        log.info("Listening on the port {}.", httpServerPort);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(httpServerPort)
                .<Void>mapEmpty()
                .onComplete(startPromise);
    }

    @Override
    public void stop() {
        log.info("The server on the port {} has been stopped.", configuration.getHttpServerPort());
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        SystemConfig config;
        try {
            config = new SystemConfig(System.getenv());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }
        vertx.deployVerticle(new OAuthProxyVerticle(config));
    }
}
