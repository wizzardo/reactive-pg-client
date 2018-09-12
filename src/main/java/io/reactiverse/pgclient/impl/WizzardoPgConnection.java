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

import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.pgclient.PgNotification;
import io.reactiverse.pgclient.PgPreparedQuery;
import io.reactiverse.pgclient.PgTransaction;
import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WizzardoPgConnection extends PgClientBase<WizzardoPgConnection> implements PgConnection, Connection.Holder {

    public final Connection conn;
    private volatile Handler<Throwable> exceptionHandler;
    private volatile Handler<Void> closeHandler;
    private Transaction tx;
    private volatile Handler<PgNotification> notificationHandler;

    public WizzardoPgConnection(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Connection connection() {
        return conn;
    }

    @Override
    public void handleClosed() {
        Handler<Void> handler = closeHandler;
        if (handler != null) {
            handler.handle(null);
        }
    }

    @Override
    protected void schedule(CommandBase<?> cmd) {
        if (tx != null) {
            tx.schedule(cmd);
        } else {
            conn.schedule(cmd);
        }
    }

    @Override
    public void handleException(Throwable err) {
        Handler<Throwable> handler = exceptionHandler;
        if (handler != null) {
            handler.handle(err);
        } else {
            err.printStackTrace();
        }
    }

    @Override
    public boolean isSSL() {
        return conn.isSsl();
    }

    @Override
    public PgConnection closeHandler(Handler<Void> handler) {
        closeHandler = handler;
        return this;
    }

    @Override
    public PgConnection notificationHandler(Handler<PgNotification> handler) {
        notificationHandler = handler;
        return this;
    }

    @Override
    public PgConnection exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        return this;
    }

    @Override
    public PgTransaction begin() {
      return begin(false);
    }

    @Override
    public PgTransaction begin(boolean closeOnEnd) {
      if (tx != null) {
        throw new IllegalStateException();
      }
      tx = new Transaction(h -> h.handle(null), conn, v -> {
        tx = null;
        if (closeOnEnd) {
          close();
        }
      });
      return tx;
    }

    public void handleNotification(int processId, String channel, String payload) {
        Handler<PgNotification> handler = notificationHandler;
        if (handler != null) {
            handler.handle(new PgNotification().setProcessId(processId).setChannel(channel).setPayload(payload));
        }
    }

    @Override
    public void close() {
        if (tx != null) {
            tx.rollback(ar -> conn.close(this));
            tx = null;
        } else {
            conn.close(this);
        }
    }

    @Override
    public PgConnection prepare(String sql, Handler<AsyncResult<PgPreparedQuery>> handler) {
        schedule(new PrepareStatementCommand(sql, ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(new PgPreparedQueryImpl(conn, h -> h.handle(null), ar.result())));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        }));
        return this;
    }
}