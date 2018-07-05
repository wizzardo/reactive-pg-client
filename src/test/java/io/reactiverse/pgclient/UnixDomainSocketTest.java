/*
 * Copyright (C) 2018 Julien Viet
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
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

@RunWith(VertxUnitRunner.class)
public class UnixDomainSocketTest {

  private static VertxPgConnectOptions options;
  private PgPool client;

  @BeforeClass
  public static void beforeClass() throws Exception {
    Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
    boolean nativeTransportEnabled = vertx.isNativeTransportEnabled();
    vertx.close();
    if (nativeTransportEnabled) {
      options = PgTestBase.startPg(true);
      assertTrue(options.isUsingDomainSocket());
    }
  }

  @AfterClass
  public static void afterClass() throws Exception {
    PgTestBase.stopPg();
  }

  @Before
  public void before() {
  }

  @After
  public void after() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  public void uriTest(TestContext context) {
    assumeNotNull(options);
    String uri = "postgresql://postgres:postgres@/postgres?host=" + options.getHost() + "&port=" + options.getPort();
    client = VertxPgClientFactory.pool(uri);
    client.getConnection(AsyncResultVertxConverter.from(context.asyncAssertSuccess(pgConnection -> pgConnection.close())));
  }

  @Test
  public void simpleConnect(TestContext context) {
    assumeNotNull(options);
    client = VertxPgClientFactory.pool(new VertxPgPoolOptions(options));
    client.getConnection(AsyncResultVertxConverter.from(context.asyncAssertSuccess(pgConnection -> pgConnection.close())));
  }

  @Test
  public void connectWithVertxInstance(TestContext context) {
    assumeNotNull(options);
    Vertx vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true));
    try {
      client = VertxPgClientFactory.pool(vertx, new VertxPgPoolOptions(options));
      Async async = context.async();
      client.getConnection(AsyncResultVertxConverter.from(context.asyncAssertSuccess(pgConnection -> {
        async.complete();
        pgConnection.close();
      })));
      async.await();
    } finally {
      vertx.close();
    }
  }
}
