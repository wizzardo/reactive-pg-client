package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.PgClientFactory;
import io.reactiverse.pgclient.shared.AsyncResultVertxConverter;
import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@RunWith(ReactiverseUnitRunner.class)
public abstract class DataTypeTestBase extends PgTestBase {

  Vertx vertx;
  PgClientFactory pgClientFactory;

  protected abstract PgConnectOptions options();

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    pgClientFactory = PgClientFactory.vertx(vertx);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ar -> ctx.<Void>asyncAssertSuccess().handle(AsyncResultVertxConverter.from(ar)));
  }
}
