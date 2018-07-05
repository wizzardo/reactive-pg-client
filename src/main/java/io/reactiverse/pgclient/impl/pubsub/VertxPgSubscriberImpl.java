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
package io.reactiverse.pgclient.impl.pubsub;

import io.reactiverse.pgclient.*;
import io.reactiverse.pgclient.impl.VertxPgClientFactory;
import io.reactiverse.pgclient.pubsub.PgSubscriber;
import io.reactiverse.pgclient.pubsub.PgChannel;
import io.reactiverse.pgclient.shared.AsyncResultVertxConverter;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VertxPgSubscriberImpl implements PgSubscriber {

  private static Logger log = LoggerFactory.getLogger(VertxPgSubscriberImpl.class);
  private static final Function<Integer, Long> DEFAULT_RECONNECT_POLICY = count -> -1L;

  private final Vertx vertx;
  private final VertxPgConnectOptions options;
  private Map<String, ChannelList> channels = new HashMap<>();
  private Function<Integer, Long> reconnectPolicy = DEFAULT_RECONNECT_POLICY;

  private PgConnection conn;
  private boolean connecting;
  private boolean closed = true;
  private Handler<Void> closeHandler;

  public VertxPgSubscriberImpl(Vertx vertx, VertxPgConnectOptions options) {
    this.vertx = vertx;
    this.options = new VertxPgConnectOptions(options);
  }

  private void handleNotification(PgNotification notif) {
    List<Handler<String>> handlers = new ArrayList<>();
    synchronized (this) {
      ChannelList channel = channels.get(notif.getChannel());
      if (channel != null) {
        channel.subs.forEach(sub -> {
          if (!sub.paused) {
            Handler<String> handler = sub.eventHandler;
            if (handler != null) {
              handlers.add(handler);
            } else {
              // Race ?
            }
          }
        });
      } else {
        // Race ?
      }
    }
    handlers.forEach(handler -> {
      handler.handle(notif.getPayload());
    });
  }

  @Override
  public synchronized PgSubscriber closeHandler(io.reactiverse.pgclient.shared.Handler<Void> handler) {
    closeHandler = handler::handle;
    return this;
  }

  @Override
  public synchronized PgSubscriber reconnectPolicy(Function<Integer, Long> policy) {
    if (policy == null) {
      reconnectPolicy = DEFAULT_RECONNECT_POLICY;
    } else {
      reconnectPolicy = policy;
    }
    return this;
  }

  private synchronized void handleClose(Void v) {
    conn = null;
    checkReconnect(0);
  }

  private void checkReconnect(int count) {
    if (!closed) {
      Long val = reconnectPolicy.apply(count);
      if (val >= 0) {
        tryConnect(val, ar -> {
          if (ar.failed()) {
            checkReconnect(count + 1);
          }
        });
        return;
      }
      closed = true;
    }
    List<Handler<Void>> all = channels
      .values()
      .stream()
      .flatMap(channel -> channel.subs.stream())
      .map(sub -> sub.endHandler)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    channels.clear();
    all.forEach(handler -> handler.handle(null));
    Handler<Void> handler = closeHandler;
    if (handler != null) {
      handler.handle(null);
    }
  }

  @Override
  public synchronized boolean closed() {
    return closed;
  }

  @Override
  public synchronized PgConnection actualConnection() {
    return conn;
  }

  @Override
  public synchronized PgSubscriber connect(io.reactiverse.pgclient.shared.Handler<io.reactiverse.pgclient.shared.AsyncResult<Void>> handler) {
    if (closed) {
      closed = false;
      tryConnect(0, handler);
    }
    return this;
  }

  private void tryConnect(long delayMillis, io.reactiverse.pgclient.shared.Handler<io.reactiverse.pgclient.shared.AsyncResult<Void>> handler) {
    if (!connecting) {
      connecting = true;
      if (delayMillis > 0) {
        vertx.setTimer(delayMillis, v -> doConnect(handler));
      } else {
        doConnect(handler);
      }
    }
  }

  private void doConnect(io.reactiverse.pgclient.shared.Handler<io.reactiverse.pgclient.shared.AsyncResult<Void>> completionHandler) {
    VertxPgClientFactory.connect(vertx, options, ar -> handleConnectResult(completionHandler, ar));
  }

  private synchronized void handleConnectResult(io.reactiverse.pgclient.shared.Handler<io.reactiverse.pgclient.shared.AsyncResult<Void>> completionHandler, AsyncResult<PgConnection> ar1) {
    connecting = false;
    if (ar1.succeeded()) {
      conn = ar1.result();
      conn.notificationHandler(VertxPgSubscriberImpl.this::handleNotification);
      conn.closeHandler(VertxPgSubscriberImpl.this::handleClose);
      if (channels.size() > 0) {
        List<Handler<Void>> handlers = channels.values()
          .stream()
          .flatMap(channel -> channel.subs.stream())
          .map(sub -> sub.subscribeHandler)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
        String sql = channels.values()
          .stream()
          .map(channel -> {
            channel.subscribed = true;
            return channel.name;
          })
          .collect(Collectors.joining(";LISTEN ", "LISTEN ", ""));
        conn.query(sql, ar2 -> {
          if (ar2.failed()) {
            log.error("Cannot LISTEN to channels", ar2.cause());
            conn.close();
          } else {
            handlers.forEach(vertx::runOnContext);
          }
          completionHandler.handle(ar2.mapEmpty());
        });
        return;
      }
    }
    completionHandler.handle(AsyncResultVertxConverter.from(ar1.mapEmpty()));
  }

  private class ChannelList {

    final String name;
    final ArrayList<ChannelImpl> subs = new ArrayList<>();
    boolean subscribed;

    ChannelList(String name) {
      this.name = name;
    }

    void add(ChannelImpl sub) {
      subs.add(sub);
      if (!subscribed) {
        if (conn != null) {
          subscribed = true;
          String sql = "LISTEN " + name;
          conn.query(sql, ar -> {
            if (ar.succeeded()) {
              Handler<Void> handler = sub.subscribeHandler;
              if (handler != null) {
                handler.handle(null);
              }
            } else {
              log.error("Cannot LISTEN to channel " + name, ar.cause());
            }
          });
        }
      }
    }

    void remove(ChannelImpl sub) {
      subs.remove(sub);
      if (subs.isEmpty()) {
        channels.remove(name, this);
        if (conn != null) {
          conn.query("UNLISTEN " + name, ar -> {
            if (ar.failed()) {
              log.error("Cannot UNLISTEN channel " + name, ar.cause());
            }
          });
        }
      }
    }
  }

  private class ChannelImpl implements PgChannel {

    private final String name;
    private Handler<Void> subscribeHandler;
    private Handler<String> eventHandler;
    private Handler<Void> endHandler;
    private ChannelList channel;
    private boolean paused;

    ChannelImpl(String name) {
      this.name = name;
    }

    @Override
    public PgChannel subscribeHandler(io.reactiverse.pgclient.shared.Handler<Void> handler) {
      synchronized (VertxPgSubscriberImpl.this) {
        subscribeHandler = handler::handle;
      }
      return this;
    }

    @Override
    public ChannelImpl exceptionHandler(io.reactiverse.pgclient.shared.Handler<Throwable> handler) {
      return this;
    }

    @Override
    public ChannelImpl handler(io.reactiverse.pgclient.shared.Handler<String> handler) {
      synchronized (VertxPgSubscriberImpl.this) {
        if (handler != null) {
          eventHandler = handler::handle;
          if (channel == null) {
            channel = channels.computeIfAbsent(name, ChannelList::new);
            channel.add(this);
          }
        } else {
          if (channel != null) {
            ChannelList ch = channel;
            channel = null;
            ch.remove(this);
            Handler<Void> _handler = endHandler;
            if (_handler != null) {
              _handler.handle(null);
            }
          }
        }
      }
      return this;
    }

    @Override
    public ChannelImpl endHandler(io.reactiverse.pgclient.shared.Handler<Void> handler) {
      synchronized (VertxPgSubscriberImpl.this) {
        endHandler = handler::handle;
      }
      return this;
    }

    @Override
    public ChannelImpl pause() {
      synchronized (VertxPgSubscriberImpl.this) {
        paused = true;
      }
      return this;
    }

    @Override
    public ChannelImpl resume() {
      synchronized (VertxPgSubscriberImpl.this) {
        paused = false;
      }
      return this;
    }
  }

  @Override
  public void close() {
    synchronized (VertxPgSubscriberImpl.this) {
      if (!closed) {
        closed = true;
        if (conn != null) {
          conn.close();
        }
      }
    }
  }

  @Override
  public PgChannel channel(String name) {
    return new ChannelImpl(name);
  }
}
