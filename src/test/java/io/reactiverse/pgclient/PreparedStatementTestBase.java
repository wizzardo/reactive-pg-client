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
import io.reactiverse.pgclient.impl.codec.util.Util;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class PreparedStatementTestBase extends PgTestBase {

  Vertx vertx;

  protected abstract PgConnectOptions options();

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testQuery1Param(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of(1), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(1, results.size());
          Tuple row = results.iterator().next();
          ctx.assertEquals(1, row.getInteger(0));
          ctx.assertEquals("fortune: No such file or directory", row.getString(1));
          ps.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ar -> {
            async.complete();
          })));
        })));
      })));
    }));
  }

  @Test
  public void testQuery(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of(1, 8, 4, 11, 2, 9), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(6, results.size());
          ps.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result -> {
            async.complete();
          })));
        })));
      })));
    }));
  }

  @Test
  public void testCollectorQuery(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
        ps.execute(Tuple.of(1, 8, 4, 11, 2, 9), Collectors.toList(), AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(6, results.size());
          List<Row> list = results.value();
          ctx.assertEquals(list.size(), 6);
          ctx.assertEquals(6L, list.stream().distinct().count());
          ps.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result -> {
            async.complete();
          })));
        })));
      })));
    }));
  }

/*
  @Test
  public void testQueryStream(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), (ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", ctx.asyncAssertSuccess(ps -> {
        PgQuery createStream = ps.query(1, 8, 4, 11, 2, 9);
        LinkedList<JsonArray> results = new LinkedList<>();
        createStream.exceptionHandler(ctx::fail);
        createStream.endHandler(v -> {
          ctx.assertEquals(6, results.size());
          ps.close(ctx.asyncAssertSuccess(result -> {
            async.complete();
          }));
        });
        createStream.handler(rs -> results.addAll(rs.getResults()));
      }));
    }));
  }
*/
  @Test
  public void testQueryParseError(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("invalid", AsyncResultVertxConverter.from(ctx.asyncAssertFailure(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals(ErrorCodes.syntax_error, pgErr.getCode());
        async.complete();
      })));
    }));
  }

  @Test
  public void testQueryBindError(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune WHERE id=$1", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
        try {
          ps.execute(Tuple.of("invalid-id"), ar -> {});
        } catch (IllegalArgumentException e) {
          ctx.assertEquals(Util.buildInvalidArgsError(Stream.of("invalid-id"), Stream.of(Integer.class)), e.getMessage());
          async.complete();
        }
      })));
    }));
  }

  // Need to test partial query close or abortion ?
  @Test
  public void testQueryCursor(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
          PgCursor query = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          query.read(4, AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result -> {
            ctx.assertNotNull(result.columnsNames());
            ctx.assertEquals(4, result.size());
            ctx.assertTrue(query.hasMore());
            query.read(4, AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result2 -> {
              ctx.assertNotNull(result.columnsNames());
              ctx.assertEquals(4, result.size());
              ctx.assertFalse(query.hasMore());
              async.complete();
            })));
          })));
        })));
      })));
    }));
  }

  @Test
  public void testQueryCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
          PgCursor query = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          query.read(4, AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(results -> {
            ctx.assertEquals(4, results.size());
            query.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(v1 -> {
              ps.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              })));
            })));
          })));
        })));
      })));
    }));
  }

  @Test
  public void testQueryStreamCloseCursor(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune WHERE id=$1 OR id=$2 OR id=$3 OR id=$4 OR id=$5 OR id=$6", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
          PgCursor stream = ps.cursor(Tuple.of(1, 8, 4, 11, 2, 9));
          stream.read(4, AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(4, result.size());
            stream.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(v1 -> {
              ps.close(AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(v2 -> {
                async.complete();
              })));
            })));
          })));
        })));
      })));
    }));
  }

  @Test
  public void testStreamQuery(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
          PgStream<Row> stream = ps.createStream(4, Tuple.tuple());
          List<Tuple> rows = new ArrayList<>();
          AtomicInteger ended = new AtomicInteger();
          stream.endHandler(v -> {
            ctx.assertEquals(0, ended.getAndIncrement());
            ctx.assertEquals(12, rows.size());
            async.complete();
          });
          stream.handler(tuple -> {
            ctx.assertEquals(0, ended.get());
            rows.add(tuple);
          });
        })));
      })));
    }));
  }

  @Test
  public void testStreamQueryPauseInBatch(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
          PgStream<Row> stream = ps.createStream(4, Tuple.tuple());
          List<Tuple> rows = new ArrayList<>();
          AtomicInteger ended = new AtomicInteger();
          stream.endHandler(v -> {
            ctx.assertEquals(0, ended.getAndIncrement());
            ctx.assertEquals(12, rows.size());
            async.complete();
          });
          stream.handler(tuple -> {
            rows.add(tuple);
            if (rows.size() == 2) {
              stream.pause();
              vertx.setTimer(100, v -> {
                stream.resume();
              });
            }
          });
        })));
      })));
    }));
  }

  @Test
  public void testStreamQueryError(TestContext ctx) {
    Async async = ctx.async();
    VertxPgClientFactory.connect(vertx, (VertxPgConnectOptions) options(), ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM Fortune", AsyncResultVertxConverter.from(ctx.asyncAssertSuccess(ps -> {
        PgStream<Row> stream = ps.createStream(4, Tuple.tuple());
        stream.endHandler(v -> ctx.fail());
        AtomicInteger rowCount = new AtomicInteger();
        stream.exceptionHandler(err -> {
          ctx.assertEquals(4, rowCount.getAndIncrement());
          async.complete();
        });
        stream.handler(tuple -> rowCount.incrementAndGet());
      })));
    }));
  }
/*
  @Test
  public void testStreamQueryCancel(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options(), (ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(begin -> {
        conn.prepare("SELECT * FROM Fortune", ctx.asyncAssertSuccess(ps -> {
          PgStream<Tuple> createStream = ps.createStream(Tuple.tuple());
          AtomicInteger count = new AtomicInteger();
          createStream.handler(tuple -> {
            ctx.assertEquals(0, count.getAndIncrement());
            createStream.handler(null);
          });
        }));
      }));
    }));
  }
  */
}
