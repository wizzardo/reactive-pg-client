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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgConnectOptions;

public class WizzardoPgConnectOptions implements PgConnectOptions {

    public static final String DEFAULT_HOST = "localhost";
    public static int DEFAULT_PORT = 5432;
    public static final String DEFAULT_DATABASE = "db";
    public static final String DEFAULT_USER = "user";
    public static final String DEFAULT_PASSWORD = "pass";
    public static final boolean DEFAULT_CACHE_PREPARED_STATEMENTS = false;
    public static final int DEFAULT_PIPELINING_LIMIT = 256;

    private String host;
    private int port;
    private String database;
    private String user;
    private String password;
    private boolean cachePreparedStatements;
    private int pipeliningLimit;

    public WizzardoPgConnectOptions() {
        init();
    }

    public WizzardoPgConnectOptions(WizzardoPgConnectOptions other) {
        host = other.host;
        port = other.port;
        database = other.database;
        user = other.user;
        password = other.password;
        pipeliningLimit = other.pipeliningLimit;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public WizzardoPgConnectOptions setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public WizzardoPgConnectOptions setPort(int port) {
        this.port = port;
        return this;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public WizzardoPgConnectOptions setDatabase(String database) {
        this.database = database;
        return this;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public WizzardoPgConnectOptions setUser(String user) {
        this.user = user;
        return this;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public WizzardoPgConnectOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public int getPipeliningLimit() {
        return pipeliningLimit;
    }

    @Override
    public PgConnectOptions setPipeliningLimit(int pipeliningLimit) {
        if (pipeliningLimit < 1) {
            throw new IllegalArgumentException();
        }
        this.pipeliningLimit = pipeliningLimit;
        return this;
    }

    @Override
    public boolean getCachePreparedStatements() {
        return cachePreparedStatements;
    }

    @Override
    public PgConnectOptions setCachePreparedStatements(boolean cachePreparedStatements) {
        this.cachePreparedStatements = cachePreparedStatements;
        return this;
    }


    /**
     * Initialize with the default options.
     */
    private void init() {
        host = DEFAULT_HOST;
        port = DEFAULT_PORT;
        database = DEFAULT_DATABASE;
        user = DEFAULT_USER;
        password = DEFAULT_PASSWORD;
        cachePreparedStatements = DEFAULT_CACHE_PREPARED_STATEMENTS;
        pipeliningLimit = DEFAULT_PIPELINING_LIMIT;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WizzardoPgConnectOptions)) return false;
        if (!super.equals(o)) return false;

        WizzardoPgConnectOptions that = (WizzardoPgConnectOptions) o;

        if (!host.equals(that.host)) return false;
        if (port != that.port) return false;
        if (!database.equals(that.database)) return false;
        if (!user.equals(that.user)) return false;
        if (!password.equals(that.password)) return false;
        if (cachePreparedStatements != that.cachePreparedStatements) return false;
        if (pipeliningLimit != that.pipeliningLimit) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port;
        result = 31 * result + database.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + (cachePreparedStatements ? 1 : 0);
        result = 31 * result + pipeliningLimit;
        return result;
    }

    public boolean isUsingDomainSocket() {
        return this.getHost().startsWith("/");
    }
}
