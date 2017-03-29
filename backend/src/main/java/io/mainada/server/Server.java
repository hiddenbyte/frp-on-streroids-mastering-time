package io.mainada.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import rx.Single;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

public class Server extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8080;

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        final Router router = Router.router(vertx);

        router.route().handler(CorsHandler.create("*"));
        router.route().handler(this::handleRequest);

        server
                .requestHandler(router::accept)
                .rxListen(PORT)
                .subscribe(
                        success -> LOGGER.info("Server started successfully."),
                        error -> LOGGER.error("Server failed to start.", error)
                );
    }

    private void handleRequest(final RoutingContext req) {
        LOGGER.info("Received cell.");

        final HttpServerResponse resp = req.response()
                .setChunked(true);

        req.request().toObservable()
                .map(Buffer::toJsonObject)
                .map(this::sendEventBusMessage)
                .flatMap(Single::toObservable)
                .map(Message::body)
                .map(JsonObject::encode)
                .subscribe(
                        resp::write,
                        error -> {
                            LOGGER.error("Failed to handle request.", error);
                            resp.setStatusCode(BAD_REQUEST.code()).end();
                        },
                        resp::end
                );
    }

    private Single<Message<JsonObject>> sendEventBusMessage(final JsonObject body) {
        final JsonArray parameters = body.getJsonArray("parameters", new JsonArray());
        final String function = body.getString("function");
        return vertx.eventBus().rxSend(function, parameters);
    }
}
