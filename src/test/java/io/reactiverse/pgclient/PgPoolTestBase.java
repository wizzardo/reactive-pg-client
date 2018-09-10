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

import io.reactiverse.pgclient.shared.AsyncResultVertxConverter;
import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(ReactiverseUnitRunner.class)
public abstract class PgPoolTestBase extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ar -> ctx.<Void>asyncAssertSuccess().handle(AsyncResultVertxConverter.from(ar)));
  }

  protected abstract PgPool createPool(VertxPgConnectOptions options, int size);

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, 4);
    for (int i = 0;i < num;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            PgResult result = ar.result();
            ctx.assertEquals(10000, result.size());
          } else {
            ctx.assertEquals("closed", ar.cause().getMessage());
          }
          conn.close();
          async.countDown();
        });
      }));
    }
  }

  @Test
  public void testQuery(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, 4);
    for (int i = 0;i < num;i++) {
      pool.query("SELECT id, randomnumber from WORLD", ar -> {
        if (ar.succeeded()) {
          PgResult result = ar.result();
          ctx.assertEquals(10000, result.size());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testQueryWithParams(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, 4);
    for (int i = 0;i < num;i++) {
      pool.preparedQuery("SELECT id, randomnumber from WORLD where id=$1", Tuple.of(i + 1), ar -> {
        if (ar.succeeded()) {
          PgResult result = ar.result();
          ctx.assertEquals(1, result.size());
        } else {
          ar.cause().printStackTrace();
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testUpdate(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, 4);
    for (int i = 0;i < num;i++) {
      pool.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ar -> {
        if (ar.succeeded()) {
          PgResult result = ar.result();
          ctx.assertEquals(1, result.updatedCount());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testUpdateWithParams(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, 4);
    for (int i = 0;i < num;i++) {
      pool.preparedQuery("UPDATE Fortune SET message = 'Whatever' WHERE id = $1", Tuple.of(9), ar -> {
        if (ar.succeeded()) {
          PgResult result = ar.result();
          ctx.assertEquals(1, result.updatedCount());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testReconnect(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ar -> ctx.<Void>asyncAssertSuccess(v1 -> {
      PgPool pool = createPool(new VertxPgConnectOptions(options).setPort(8080).setHost("localhost"), 1);
      pool.getConnection(ctx.asyncAssertSuccess(conn1 -> {
        proxyConn.get().close();
        conn1.closeHandler(v2 -> {
          conn1.query("never-read", ctx.asyncAssertFailure(err -> {
            pool.getConnection(ctx.asyncAssertSuccess(conn2 -> {
              conn2.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v3 -> {
                async.complete();
              }));
            }));
          }));
        });
      }));
    }).handle(AsyncResultVertxConverter.from(ar)));
  }
}
