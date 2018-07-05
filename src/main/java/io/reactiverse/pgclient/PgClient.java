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

import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Handler;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;

import java.util.List;
import java.util.stream.Collector;

/**
 * Defines the client operations with a Postgres Database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
//@VertxGen
public interface PgClient {

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgClient preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgRowSet>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> PgClient preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

}
