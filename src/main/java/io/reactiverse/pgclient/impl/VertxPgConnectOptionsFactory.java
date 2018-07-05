package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.VertxPgConnectOptions;
import io.vertx.core.json.JsonObject;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

public class VertxPgConnectOptionsFactory {
    /**
     * Provide a {@link PgConnectOptions} configured from a connection URI.
     *
     * @param connectionUri the connection URI to configure from
     * @return a {@link PgConnectOptions} parsed from the connection URI
     * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
     */
    public static VertxPgConnectOptions fromUri(String connectionUri) throws IllegalArgumentException {
      JsonObject parsedConfiguration = PgConnectionUriParser.parse(connectionUri);
      return new VertxPgConnectOptions(parsedConfiguration);
    }

    /**
     * Provide a {@link PgConnectOptions} configured with environment variables, if the environment variable
     * is not set, then a default value will take precedence over this.
     */
    public static VertxPgConnectOptions fromEnv() {
      VertxPgConnectOptions pgConnectOptions = new VertxPgConnectOptions();

      if (getenv("PGHOSTADDR") == null) {
        if (getenv("PGHOST") != null) {
          pgConnectOptions.setHost(getenv("PGHOST"));
        }
      } else {
        pgConnectOptions.setHost(getenv("PGHOSTADDR"));
      }

      if (getenv("PGPORT") != null) {
        try {
          pgConnectOptions.setPort(parseInt(getenv("PGPORT")));
        } catch (NumberFormatException e) {
          // port will be set to default
        }
      }

      if (getenv("PGDATABASE") != null) {
        pgConnectOptions.setDatabase(getenv("PGDATABASE"));
      }
      if (getenv("PGUSER") != null) {
        pgConnectOptions.setUser(getenv("PGUSER"));
      }
      if (getenv("PGPASSWORD") != null) {
        pgConnectOptions.setPassword(getenv("PGPASSWORD"));
      }
      return pgConnectOptions;
    }
}
