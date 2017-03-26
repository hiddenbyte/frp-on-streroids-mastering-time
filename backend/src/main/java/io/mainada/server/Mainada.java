package io.mainada.server;

import io.reactivex.netty.protocol.http.server.HttpServer;

import static rx.Observable.just;

public class Mainada {
    public static void main(final String[] args) {
        HttpServer.newServer(8080).start((req, resp) -> resp.writeString(just("Hello World!")));
    }
}
