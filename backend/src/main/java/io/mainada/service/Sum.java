package io.mainada.service;

import io.reactivex.Flowable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import rx.schedulers.Schedulers;

public class Sum extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sum.class);

    @Override
    public void start() {
        final MessageConsumer<JsonArray> consumer = vertx.eventBus().consumer("sum");

        consumer.toObservable()
                .observeOn(Schedulers.io())
                .subscribe(
                        this::handleMessage,
                        error -> LOGGER.error("Something went wrong with the sum.", error),
                        consumer::unregister
                );
    }

    private void handleMessage(final Message<JsonArray> message) {
        final JsonArray body = message.body();

        Flowable.fromIterable(body)
                .cast(Double.class)
                .reduce(0.0d, (acc, ele) -> acc + ele)
                .map(result -> new JsonObject().put("result", result))
                .subscribe(message::reply, error -> LOGGER.error("Something went wrong during the sum.", error));
    }
}
