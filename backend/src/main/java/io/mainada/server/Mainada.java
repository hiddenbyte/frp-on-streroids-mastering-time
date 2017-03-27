package io.mainada.server;

import io.mainada.domain.Cell;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import rx.Observable;
import rx.Subscriber;

import java.util.HashMap;
import java.util.Map;

public class Mainada extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mainada.class);

    private Map<String, Observable<Cell>> cellObservables;
    private Map<String, Subscriber<? super Cell>> cellSubscribers;

    @Override
    public void start() {
        this.cellObservables = new HashMap<>();
        this.cellSubscribers = new HashMap<>();

        HttpServer server = vertx.createHttpServer();

        vertx.exceptionHandler(error -> LOGGER.error("Unexpected exception.", error));

        server.requestStream()
                .toObservable()
                .subscribe(this::handleRequest);

        server
                .rxListen(8080)
                .subscribe(success -> LOGGER.info("Server started successfully."), error -> LOGGER.error("Unexpected exception.", error));
    }

    private void handleRequest(final HttpServerRequest req) {
        LOGGER.info("Received cell.");

        final HttpServerResponse resp = req.response()
                .setChunked(true);

        req.toObservable()
                .map(Buffer::toJsonObject)
                .map(Cell::new)
                .flatMap(this::addCell)
                .map(a -> a)
                .map(Cell::toJsonObject)
                .map(JsonObject::toString)
                .subscribe(resp::write,
                        error -> LOGGER.error("Unexpected exception.", error),
                        resp::end);
    }

    private Observable<Cell> addCell(final Cell newCell) {
        LOGGER.info("Adding cell with id " + newCell.getId() + ".");

        return addCellObservable(newCell)
                .flatMap(this::handleNewCell)
                .doOnNext(t -> cellSubscribers.get(newCell.getId()).onNext(t));
    }

    private Observable<Cell> addCellObservable(final Cell newCell) {
        Observable<Cell> cellObservable = cellObservables.get(newCell.getId());

        if (cellObservable == null) {
            cellObservable = Observable.<Cell>unsafeCreate(subscriber -> {
                subscriber.onNext(newCell);
                cellSubscribers.put(newCell.getId(), subscriber);
            }).cache();

            cellObservable.subscribe();

            cellObservables.put(newCell.getId(), cellObservable);
        }

        return Observable.just(newCell);
    }

    private Observable<Cell> handleNewCell(final Cell newCell) {
        final Observable<Double> functionArgumentValues = getFunctionArgumentValues(newCell)
                .map(Cell::getValue);

        return newCell.getFunction()
                .map(function -> function.apply(functionArgumentValues))
                .map(result -> result.map(newCell::withValue))
                .orElse(Observable.just(newCell));
    }

    private Observable<Cell> getFunctionArgumentValues(final Cell newCell) {
        return newCell.getFunctionArguments().stream()
                .map(cellObservables::get)
                .reduce(Observable.empty(), Observable::merge);
    }
}
