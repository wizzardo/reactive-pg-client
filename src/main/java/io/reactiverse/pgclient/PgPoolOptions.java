package io.reactiverse.pgclient;


import io.reactiverse.pgclient.impl.VertxPgConnectOptionsFactory;

public interface PgPoolOptions extends PgConnectOptions {
    /**
     * Provide a {@link PgPoolOptions} configured from a connection URI.
     *
     * @param connectionUri the connection URI to configure from
     * @return a {@link PgPoolOptions} parsed from the connection URI
     * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
     */
    static VertxPgPoolOptions fromUri(String connectionUri) throws IllegalArgumentException {
      return new VertxPgPoolOptions(VertxPgConnectOptionsFactory.fromUri(connectionUri));
    }

    /**
     * Provide a {@link PgPoolOptions} configured with environment variables, if the environment variable
     * is not set, then a default value will take precedence over this.
     */
    static VertxPgPoolOptions fromEnv() {
      return new VertxPgPoolOptions(VertxPgConnectOptionsFactory.fromEnv());
    }

    int getMaxSize();

    PgPoolOptions setMaxSize(int maxSize);
}
