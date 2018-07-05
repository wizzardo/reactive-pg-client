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
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@RunWith(VertxUnitRunner.class)
public class PreparedBatchTest extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testInsert(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(79991, "batch one"));
      batch.add(Tuple.of(79992, "batch two"));
      batch.add(Tuple.of(79993, "batch three"));
      batch.add(Tuple.of(79994, "batch four"));
      conn.preparedBatch("INSERT INTO Fortune (id, message) VALUES ($1, $2)", batch, AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(79991), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar1 -> {
          ctx.assertEquals(1, ar1.size());
          Row one = ar1.iterator().next();
          ctx.assertEquals(79991, one.getInteger("id"));
          ctx.assertEquals("batch one", one.getString("message"));
          conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(79992), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar2 -> {
            ctx.assertEquals(1, ar2.size());
            Row two = ar2.iterator().next();
            ctx.assertEquals(79992, two.getInteger("id"));
            ctx.assertEquals("batch two", two.getString("message"));
            conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(79993), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar3 -> {
              ctx.assertEquals(1, ar3.size());
              Row three = ar3.iterator().next();
              ctx.assertEquals(79993, three.getInteger("id"));
              ctx.assertEquals("batch three", three.getString("message"));
              conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(79994), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar4 -> {
                ctx.assertEquals(1, ar4.size());
                Row four = ar4.iterator().next();
                ctx.assertEquals(79994, four.getInteger("id"));
                ctx.assertEquals("batch four", four.getString("message"));
                async.complete();
              })));
            })));
          })));
        })));
      })));
    }));
  }

  @Test
  public void testInsertWithFunction(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(78881, "batch one"));
      batch.add(Tuple.of(78882, "batch two"));
      batch.add(Tuple.of(78883, "batch three"));
      batch.add(Tuple.of(78884, "batch four"));
      conn.preparedBatch("INSERT INTO Fortune (id, message) VALUES ($1, upper($2))", batch, AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.updatedCount());
        conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(78881), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar1 -> {
          ctx.assertEquals(1, ar1.size());
          Row one = ar1.iterator().next();
          ctx.assertEquals(78881, one.getInteger("id"));
          ctx.assertEquals("BATCH ONE", one.getString("message"));
          conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(78882), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar2 -> {
            ctx.assertEquals(1, ar2.size());
            Row two = ar2.iterator().next();
            ctx.assertEquals(78882, two.getInteger("id"));
            ctx.assertEquals("BATCH TWO", two.getString("message"));
            conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(78883), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar3 -> {
              ctx.assertEquals(1, ar3.size());
              Row three = ar3.iterator().next();
              ctx.assertEquals(78883, three.getInteger("id"));
              ctx.assertEquals("BATCH THREE", three.getString("message"));
              conn.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(78884), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar4 -> {
                ctx.assertEquals(1, ar4.size());
                Row four = ar4.iterator().next();
                ctx.assertEquals(78884, four.getInteger("id"));
                ctx.assertEquals("BATCH FOUR", four.getString("message"));
                async.complete();
              })));
            })));
          })));
        })));
      })));
    }));
  }
}
