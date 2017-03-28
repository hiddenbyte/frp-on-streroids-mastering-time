package io.mainada.client;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;
import rx.schedulers.Schedulers;

public class Product extends AbstractVerticle {

    private static final String PRODUCT_ADDRESS = "prod";

    private static final Logger LOGGER = LoggerFactory.getLogger(Product.class);

    private WebClient webClient;
    private MessageConsumer<JsonArray> consumer;

    @Override
    public void start() {
        initConsumer();
        initWebClient();

        consumer.toObservable()
                .observeOn(Schedulers.io())
                .subscribe(
                        this::handleMessage,
                        error -> LOGGER.error("Something went wrong with the sum.", error),
                        consumer::unregister
                );
    }

    private void handleMessage(final Message<JsonArray> message) {
        webClient.post(8181, "localhost", "/")
                .rxSendJson(message.body())
                .map(HttpResponse::bodyAsJsonObject)
                .subscribe(
                        message::reply,
                        error -> LOGGER.error("Something went wrong during the sum.", error)
                );
    }

    private void initConsumer() {
        this.consumer = vertx.eventBus().consumer(PRODUCT_ADDRESS);
    }

    private void initWebClient() {
        final WebClientOptions options = new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(8181);

        this.webClient = WebClient.create(vertx, options);
    }
}
