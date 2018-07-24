package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.*;
import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;

public class WizzardoPgClientFactory {

    /**
     * Create a connection pool to the database configured with the given {@code options}.
     *
     * @param options the options for creating the pool
     * @return the connection pool
     */
    public static PgPool pool(WizzardoPgPoolOptions options) {
        return new WizzardoPgPool(options);
    }

    /**
     * Connects to the database and returns the connection if that succeeds.
     * <p/>
     * The connection interracts directly with the database is not a proxy, so closing the
     * connection will close the underlying connection to the database.
     *
     * @param options the connect options
     * @param handler the handler called with the connection or the failure
     */
    public static void connect(WizzardoPgConnectOptions options, Handler<AsyncResult<PgConnection>> handler) {
        WizzardoPgConnectionFactory client = new WizzardoPgConnectionFactory(options);
        client.connect(ar -> {
            if (ar.succeeded()) {
                Connection conn = ar.result();
                WizzardoPgConnection p = new WizzardoPgConnection(conn);
                conn.init(p);
                handler.handle(Future.succeededFuture(p));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }
}
