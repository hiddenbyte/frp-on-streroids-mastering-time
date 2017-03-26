package io.mainada.server;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServer;

import static rx.Observable.just;

public class Mainada {
    public static void main(final String[] args) {
        final HttpServer<ByteBuf, ByteBuf> server = HttpServer.newServer(8080);

        server.start((req, resp) -> resp.writeString(just("Hello World!")));

        server.awaitShutdown();
    }
}
