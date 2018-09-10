package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.pgclient.VertxPgConnectOptions;
import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.AsyncResultVertxConverter;
import io.reactiverse.pgclient.shared.Handler;
import io.vertx.core.Vertx;

public interface PgClientFactory {

  void connect(PgConnectOptions options, Handler<AsyncResult<PgConnection>> handler);

  static PgClientFactory vertx(Vertx vertx) {
    return (options, handler) -> VertxPgClientFactory.connect(
      vertx
      , (VertxPgConnectOptions) options
      , ar -> handler.handle(AsyncResultVertxConverter.from(ar))
    );
  }
}
