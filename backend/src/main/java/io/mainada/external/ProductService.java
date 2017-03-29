package io.mainada.external;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;

import static rx.Observable.just;

public class ProductService extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        server.requestStream()
                .toObservable()
                .subscribe(this::handleRequest);

        server.rxListen(8181)
                .subscribe(success -> LOGGER.info("ProductService started successfully."), error -> LOGGER.error("Unexpected exception.", error));
    }

    private void handleRequest(final HttpServerRequest req) {
        final HttpServerResponse resp = req.response()
                .setChunked(true);

        req.toObservable()
                .flatMapIterable(Buffer::toJsonArray)
                .cast(String.class)
                .map(Double::valueOf)
                .onErrorResumeNext(error -> {
                    LOGGER.error("Received an invalid number");
                    return just(1.0);
                })
                .reduce(1.0, (acc, ele) -> acc * ele)
                .map(result -> new JsonObject().put("result", result))
                .map(JsonObject::encode)
                .subscribe(
                        resp::write,
                        error -> LOGGER.error("Unexpected exception.", error),
                        resp::end
                );
    }
}
