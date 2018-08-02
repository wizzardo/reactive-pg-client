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

import io.reactiverse.pgclient.PgPoolOptions;

/**
 * The options for configuring a connection pool.
 *
 */
public class WizzardoPgPoolOptions extends WizzardoPgConnectOptions implements PgPoolOptions {

  public static final int DEFAULT_MAX_POOL_SIZE = 4;

  private int maxSize = DEFAULT_MAX_POOL_SIZE;

  public WizzardoPgPoolOptions() {
  }

  public WizzardoPgPoolOptions(WizzardoPgPoolOptions other) {
    super(other);
    maxSize = other.maxSize;
  }
  public WizzardoPgPoolOptions(WizzardoPgConnectOptions other) {
    super(other);
  }

  @Override
  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public WizzardoPgPoolOptions setMaxSize(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size cannot be negative");
    }
    this.maxSize = maxSize;
    return this;
  }

  @Override
  public WizzardoPgPoolOptions setHost(String host) {
    return (WizzardoPgPoolOptions) super.setHost(host);
  }

  @Override
  public WizzardoPgPoolOptions setPort(int port) {
    return (WizzardoPgPoolOptions) super.setPort(port);
  }

  @Override
  public WizzardoPgPoolOptions setDatabase(String database) {
    return (WizzardoPgPoolOptions) super.setDatabase(database);
  }

  @Override
  public WizzardoPgPoolOptions setUser(String user) {
    return (WizzardoPgPoolOptions) super.setUser(user);
  }

  @Override
  public WizzardoPgPoolOptions setPassword(String password) {
    return (WizzardoPgPoolOptions) super.setPassword(password);
  }

  @Override
  public WizzardoPgPoolOptions setPipeliningLimit(int pipeliningLimit) {
    return (WizzardoPgPoolOptions) super.setPipeliningLimit(pipeliningLimit);
  }

  @Override
  public WizzardoPgPoolOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (WizzardoPgPoolOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WizzardoPgPoolOptions)) return false;
    if (!super.equals(o)) return false;

    WizzardoPgPoolOptions that = (WizzardoPgPoolOptions) o;

    if (maxSize != that.maxSize) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + maxSize;
    return result;
  }
}
