/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.*;
import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class PgPoolImpl extends PgClientBase<PgPoolImpl> implements PgPool {

  private final Context context;
  private final VertxPgConnectionFactory factory;
  private final ConnectionPool pool;
  private final boolean closeVertx;

  public PgPoolImpl(Vertx vertx, boolean closeVertx, VertxPgPoolOptions options) {
    int maxSize = options.getMaxSize();
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    if (options.isUsingDomainSocket() && !vertx.isNativeTransportEnabled()) {
      throw new VertxException("Native transport is not available");
    }
    this.context = vertx.getOrCreateContext();
    this.factory = new VertxPgConnectionFactory(context, Vertx.currentContext() != null, options);
    this.pool = new ConnectionPool(completionHandler -> factory.connect(completionHandler::handle), maxSize);
    this.closeVertx = closeVertx;
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pool.acquire(new ConnectionWaiter(handler));
    } else {
      context.runOnContext(v -> getConnection(handler));
    }
  }

  @Override
  protected void schedule(CommandBase<?> cmd) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pool.acquire(new CommandWaiter() {
        @Override
        protected void onSuccess(Connection conn) {
          // Work around stack over flow
          context.runOnContext(v -> {
            conn.schedule(cmd);
            conn.close(this);
          });
        }
        @Override
        protected void onFailure(Throwable cause) {
          cmd.fail(cause);
        }
      });
    } else {
      context.runOnContext(v -> schedule(cmd));
    }
  }

  private abstract class CommandWaiter implements Connection.Holder, Handler<AsyncResult<Connection>> {

    private Connection conn;

    protected abstract void onSuccess(Connection conn);

    protected abstract void onFailure(Throwable cause);

    @Override
    public void handleNotification(int processId, String channel, String payload) {
      // What should we do ?
    }

    @Override
    public void handle(AsyncResult<Connection> ar) {
      if (ar.succeeded()) {
        Connection conn = ar.result();
        this.conn = conn;
        conn.init(this);
        onSuccess(conn);
      } else {
        onFailure(ar.cause());
      }
    }

    @Override
    public void handleClosed() {
    }

    @Override
    public void handleException(Throwable err) {
    }

    @Override
    public Connection connection() {
      return conn;
    }
  }

  private class ConnectionWaiter implements Handler<AsyncResult<Connection>> {

    private final Handler<AsyncResult<PgConnection>> handler;

    private ConnectionWaiter(Handler<AsyncResult<PgConnection>> handler) {
      this.handler = handler;
    }

    @Override
    public void handle(AsyncResult<Connection> ar) {
      if (ar.succeeded()) {
        Connection conn = ar.result();
        PgConnectionImpl holder = new PgConnectionImpl(context, conn);
        conn.init(holder);
        handler.handle(Future.succeededFuture(holder));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }
  }

  @Override
  public void close() {
    pool.close();
    factory.close();
    if (closeVertx) {
      context.owner().close();
    }
  }
}
