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

package io.reactiverse.pgclient.pool;

import io.reactiverse.pgclient.impl.Connection;
import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;

import java.util.ArrayDeque;
import java.util.function.Consumer;

class ConnectionQueue extends ArrayDeque<Handler<AsyncResult<Connection>>> implements Consumer<Handler<AsyncResult<Connection>>> {

  @Override
  public void accept(Handler<AsyncResult<Connection>> event) {
    add(event);
  }

  void connect(SimpleConnection conn) {
    poll().handle(Future.succeededFuture(conn));
  }
}
