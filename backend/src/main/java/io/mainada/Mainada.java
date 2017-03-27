package io.mainada;

import io.mainada.client.Product;
import io.mainada.external.ProductService;
import io.mainada.server.Server;
import io.mainada.service.Sum;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import rx.Single;

public class Mainada extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mainada.class);

    @Override
    public void start(final Future<Void> startFuture) {
        final Single<String> serverDeploy = vertx.rxDeployVerticle(Server.class.getName());
        final Single<String> sumDeploy = vertx.rxDeployVerticle(Sum.class.getName());
        final Single<String> productDeploy = vertx.rxDeployVerticle(Product.class.getName());
        final Single<String> productServiceDeploy = vertx.rxDeployVerticle(ProductService.class.getName());

        Single.zip(
                serverDeploy,
                sumDeploy,
                productDeploy,
                productServiceDeploy,
                (server, sum, product, productService) -> null
        )
                .subscribe(success -> {
                    LOGGER.info("Application deployed successfully.");
                    startFuture.complete();
                }, error -> {
                    LOGGER.info("Application failed to deploy.", error);
                    startFuture.fail(error);
                });
    }
}
