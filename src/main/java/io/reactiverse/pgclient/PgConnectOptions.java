package io.reactiverse.pgclient;

import io.reactiverse.pgclient.impl.PgConnectionUriParser;
import io.vertx.core.json.JsonObject;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

public interface PgConnectOptions {

    String getHost();

    PgConnectOptions setHost(String host);

    int getPort();

    PgConnectOptions setPort(int port);

    String getDatabase();

    PgConnectOptions setDatabase(String database);

    String getUser();

    PgConnectOptions setUser(String user);

    String getPassword();

    PgConnectOptions setPassword(String password);

    int getPipeliningLimit();

    PgConnectOptions setPipeliningLimit(int pipeliningLimit);

    boolean getCachePreparedStatements();

    PgConnectOptions setCachePreparedStatements(boolean cachePreparedStatements);

    boolean isUsingDomainSocket();
}
