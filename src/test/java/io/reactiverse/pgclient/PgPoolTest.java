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

package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.VertxPgClientFactory;
import io.reactiverse.pgclient.shared.AsyncResultVertxConverter;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPoolTest extends PgPoolTestBase {

  @Override
  protected PgPool createPool(VertxPgConnectOptions options, int size) {
    return VertxPgClientFactory.pool(vertx, new VertxPgPoolOptions(options).setMaxSize(size));
  }

  @Test
  public void testReconnectQueued(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PgPool pool = createPool(new VertxPgConnectOptions(options).setPort(8080).setHost("localhost"), 1);
      pool.getConnection(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(conn -> {
        proxyConn.get().close();
      })));
      pool.getConnection(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        })));
      })));
    }));
  }

  @Test
  public void testRunWithExisting(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      try {
        VertxPgClientFactory.pool(new VertxPgPoolOptions());
        ctx.fail();
      } catch (IllegalStateException ignore) {
        async.complete();
      }
    });
  }

  @Test
  public void testRunStandalone(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = VertxPgClientFactory.pool(new VertxPgPoolOptions(options));
    try {
      pool.query("SELECT id, randomnumber from WORLD", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(v -> {
        async.complete();
      })));
      async.await(4000);
    } finally {
      pool.close();
    }
  }
}
