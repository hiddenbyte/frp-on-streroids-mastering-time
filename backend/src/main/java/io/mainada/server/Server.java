package io.mainada.server;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import rx.Single;

public class Server extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 8080;

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        server.requestStream()
                .toObservable()
                .subscribe(this::handleRequest);

        server.rxListen(PORT)
                .subscribe(
                        success -> LOGGER.info("Server started successfully."),
                        error -> LOGGER.error("Server failed to start.", error)
                );
    }

    private void handleRequest(final HttpServerRequest req) {
        LOGGER.info("Received cell.");

        final HttpServerResponse resp = req.response()
                .setChunked(true);

        req.toObservable()
                .map(Buffer::toJsonObject)
                .map(this::sendEventBusMessage)
                .flatMap(Single::toObservable)
                .map(Message::body)
                .map(JsonObject::encode)
                .subscribe(
                        resp::write,
                        error -> LOGGER.error("Failed to handle request.", error),
                        resp::end
                );
    }

    private Single<Message<JsonObject>> sendEventBusMessage(final JsonObject body) {
        final JsonArray parameters = body.getJsonArray("parameters");
        final String function = body.getString("function");
        return vertx.eventBus().rxSend(function, parameters);
    }
}
