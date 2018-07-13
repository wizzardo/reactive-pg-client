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

import io.reactiverse.pgclient.shared.Handler;

import java.util.ArrayDeque;

public interface Connection {

  void init(Holder holder);

  boolean isSsl();

  void schedule(CommandBase<?> cmd);

  void close(Holder holder);

  void upgradeToSSL(Handler<Void> handler);

  interface Holder {

    Connection connection();

    void handleNotification(int processId, String channel, String payload);

    void handleClosed();

    void handleException(Throwable err);

  }

  class CachedPreparedStatement implements Handler<CommandResponse<PreparedStatement>> {

    private CommandResponse<PreparedStatement> resp;
    private final ArrayDeque<Handler<? super CommandResponse<PreparedStatement>>> waiters = new ArrayDeque<>();

    void get(Handler<? super CommandResponse<PreparedStatement>> handler) {
      if (resp != null) {
        handler.handle(resp);
      } else {
        waiters.add(handler);
      }
    }

    @Override
    public void handle(CommandResponse<PreparedStatement> event) {
      resp = event;
      Handler<? super CommandResponse<PreparedStatement>> waiter;
      while ((waiter = waiters.poll()) != null) {
        waiter.handle(resp);
      }
    }
  }
}
