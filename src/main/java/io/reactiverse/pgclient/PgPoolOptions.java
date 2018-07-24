package io.reactiverse.pgclient;

public interface PgPoolOptions extends PgConnectOptions {

    int getMaxSize();

    PgPoolOptions setMaxSize(int maxSize);
}
