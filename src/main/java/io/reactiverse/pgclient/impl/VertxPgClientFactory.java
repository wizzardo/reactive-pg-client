package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.*;
import io.vertx.core.*;

public class VertxPgClientFactory {
    /**
     * Like {@link #pool(VertxPgPoolOptions)} with options build from the environment variables.
     */
    public static PgPool pool() {
      return pool(PgPoolOptions.fromEnv());
    }

    /**
     * Like {@link #pool(VertxPgPoolOptions)} with options build from {@code connectionUri}.
     */
    public static PgPool pool(String connectionUri) {
      return pool(PgPoolOptions.fromUri(connectionUri));
    }

    /**
     * Like {@link #pool(Vertx, VertxPgPoolOptions)} with options build from the environment variables.
     */
    public static PgPool pool(Vertx vertx) {
      return pool(vertx, PgPoolOptions.fromEnv());
    }

    /**
     * Like {@link #pool(Vertx, VertxPgPoolOptions)} with options build from {@code connectionUri}.
     */
    public static PgPool pool(Vertx vertx, String connectionUri) {
      return pool(vertx, PgPoolOptions.fromUri(connectionUri));
    }

    /**
     * Create a connection pool to the database configured with the given {@code options}.
     *
     * @param options the options for creating the pool
     * @return the connection pool
     */
    public static PgPool pool(VertxPgPoolOptions options) {
      if (Vertx.currentContext() != null) {
        throw new IllegalStateException("Running in a Vertx context => use PgPool#pool(Vertx, PgPoolOptions) instead");
      }
      VertxOptions vertxOptions = new VertxOptions();
      if (options.isUsingDomainSocket()) {
        vertxOptions.setPreferNativeTransport(true);
      }
      Vertx vertx = Vertx.vertx(vertxOptions);
      return new PgPoolImpl(vertx, true, options);
    }

    /**
     * Like {@link #pool(VertxPgPoolOptions)} with a specific {@link Vertx} instance.
     */
    public static PgPool pool(Vertx vertx, VertxPgPoolOptions options) {
      return new PgPoolImpl(vertx, false, options);
    }

    /**
     * Connects to the database and returns the connection if that succeeds.
     * <p/>
     * The connection interracts directly with the database is not a proxy, so closing the
     * connection will close the underlying connection to the database.
     *
     * @param vertx the vertx instance
     * @param options the connect options
     * @param handler the handler called with the connection or the failure
     */
    public static void connect(Vertx vertx, VertxPgConnectOptions options, Handler<AsyncResult<PgConnection>> handler) {
      Context ctx = Vertx.currentContext();
      if (ctx != null) {
        VertxPgConnectionFactory client = new VertxPgConnectionFactory(ctx, false, options);
        client.connect(ar -> {
          if (ar.succeeded()) {
            Connection conn = ar.result();
            PgConnectionImpl p = new PgConnectionImpl(ctx, conn);
            conn.init(p);
            handler.handle(Future.succeededFuture(p));
          } else {
            handler.handle(Future.failedFuture(ar.cause()));
          }
        });
      } else {
        vertx.runOnContext(v -> {
          if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
            handler.handle(Future.failedFuture("Native transport is not available"));
          } else {
            connect(vertx, options, handler);
          }
        });
      }
    }

    /**
     * Like {@link #connect(Vertx, VertxPgConnectOptions, Handler)} with options build from the environment variables.
     */
    public static void connect(Vertx vertx, Handler<AsyncResult<PgConnection>> handler) {
      connect(vertx, VertxPgConnectOptionsFactory.fromEnv(), handler);
    }

    /**
     * Like {@link #connect(Vertx, VertxPgConnectOptions, Handler)} with options build from {@code connectionUri}.
     */
    public static void connect(Vertx vertx, String connectionUri, Handler<AsyncResult<PgConnection>> handler) {
      connect(vertx, VertxPgConnectOptionsFactory.fromUri(connectionUri), handler);
    }
}
