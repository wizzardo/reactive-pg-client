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


import com.wizzardo.epoll.EpollCore;
import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;
import io.vertx.core.impl.NetSocketInternal;

import java.io.IOException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class WizzardoPgConnectionFactory {

    private static EpollCore core;

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean cachePreparedStatements;
    private final int pipeliningLimit;
    private final boolean isUsingDomainSocket;

    static {
      core = new EpollCore();
      core.setDaemon(true);
      core.start();
    }

    public WizzardoPgConnectionFactory(WizzardoPgConnectOptions options) {
        this.host = options.getHost();
        this.port = options.getPort();
        this.database = options.getDatabase();
        this.username = options.getUser();
        this.password = options.getPassword();
        this.cachePreparedStatements = options.getCachePreparedStatements();
        this.pipeliningLimit = options.getPipeliningLimit();
        this.isUsingDomainSocket = options.isUsingDomainSocket();
    }

    public void close() {
        core.close();
    }

    public void connect(Handler<? super CommandResponse<Connection>> completionHandler) {
        if (isUsingDomainSocket) {
            throw new IllegalArgumentException("Not implemented yet: isUsingDomainSocket");
        }
        try {
            com.wizzardo.epoll.Connection socket = core.connect(host, port);
            WizzardoSocketConnection conn = new WizzardoSocketConnection(
                    socket,
                    cachePreparedStatements,
                    pipeliningLimit,
                    false);
            conn.initiateProtocolOrSsl(username, password, database, completionHandler::handle);
        } catch (IOException e) {
            completionHandler.handle(CommandResponse.failure(e));
        }
    }

}
