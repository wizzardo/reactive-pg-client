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

import io.reactiverse.pgclient.impl.VertxPgConnectOptionsFactory;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;

/**
 * The options for configuring a connection pool.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@DataObject(generateConverter = true)
public class VertxPgPoolOptions extends VertxPgConnectOptions implements PgPoolOptions {

  public static final int DEFAULT_MAX_POOL_SIZE = 4;

  private int maxSize = DEFAULT_MAX_POOL_SIZE;

  public VertxPgPoolOptions() {
  }

  public VertxPgPoolOptions(JsonObject json) {
    super(json);
    VertxPgPoolOptionsConverter.fromJson(json, this);
  }

  public VertxPgPoolOptions(VertxPgPoolOptions other) {
    super(other);
    maxSize = other.maxSize;
  }

  public VertxPgPoolOptions(VertxPgConnectOptions other) {
    super(other);
    maxSize = DEFAULT_MAX_POOL_SIZE;
  }

  /**
   * Provide a {@link PgPoolOptions} configured from a connection URI.
   *
   * @param connectionUri the connection URI to configure from
   * @return a {@link PgPoolOptions} parsed from the connection URI
   * @throws IllegalArgumentException when the {@code connectionUri} is in an invalid format
   */
  public static VertxPgPoolOptions fromUri(String connectionUri) throws IllegalArgumentException {
    return new VertxPgPoolOptions(VertxPgConnectOptionsFactory.fromUri(connectionUri));
  }

  /**
   * Provide a {@link PgPoolOptions} configured with environment variables, if the environment variable
   * is not set, then a default value will take precedence over this.
   */
  public static VertxPgPoolOptions fromEnv() {
    return new VertxPgPoolOptions(VertxPgConnectOptionsFactory.fromEnv());
  }

  @Override
  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public VertxPgPoolOptions setMaxSize(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Max size cannot be negative");
    }
    this.maxSize = maxSize;
    return this;
  }

  @Override
  public VertxPgPoolOptions setHost(String host) {
    return (VertxPgPoolOptions) super.setHost(host);
  }

  @Override
  public VertxPgPoolOptions setPort(int port) {
    return (VertxPgPoolOptions) super.setPort(port);
  }

  @Override
  public VertxPgPoolOptions setDatabase(String database) {
    return (VertxPgPoolOptions) super.setDatabase(database);
  }

  @Override
  public VertxPgPoolOptions setUser(String user) {
    return (VertxPgPoolOptions) super.setUser(user);
  }

  @Override
  public VertxPgPoolOptions setPassword(String password) {
    return (VertxPgPoolOptions) super.setPassword(password);
  }

  @Override
  public VertxPgPoolOptions setPipeliningLimit(int pipeliningLimit) {
    return (VertxPgPoolOptions) super.setPipeliningLimit(pipeliningLimit);
  }

  @Override
  public VertxPgPoolOptions setCachePreparedStatements(boolean cachePreparedStatements) {
    return (VertxPgPoolOptions) super.setCachePreparedStatements(cachePreparedStatements);
  }

  @Override
  public VertxPgPoolOptions setSendBufferSize(int sendBufferSize) {
    return (VertxPgPoolOptions) super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public VertxPgPoolOptions setReceiveBufferSize(int receiveBufferSize) {
    return (VertxPgPoolOptions) super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public VertxPgPoolOptions setReuseAddress(boolean reuseAddress) {
    return (VertxPgPoolOptions) super.setReuseAddress(reuseAddress);
  }

  @Override
  public VertxPgPoolOptions setTrafficClass(int trafficClass) {
    return (VertxPgPoolOptions) super.setTrafficClass(trafficClass);
  }

  @Override
  public VertxPgPoolOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (VertxPgPoolOptions) super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public VertxPgPoolOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (VertxPgPoolOptions) super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public VertxPgPoolOptions setSoLinger(int soLinger) {
    return (VertxPgPoolOptions) super.setSoLinger(soLinger);
  }

  @Override
  public VertxPgPoolOptions setUsePooledBuffers(boolean usePooledBuffers) {
    return (VertxPgPoolOptions) super.setUsePooledBuffers(usePooledBuffers);
  }

  @Override
  public VertxPgPoolOptions setIdleTimeout(int idleTimeout) {
    return (VertxPgPoolOptions) super.setIdleTimeout(idleTimeout);
  }

  @Override
  public VertxPgPoolOptions setSsl(boolean ssl) {
    return (VertxPgPoolOptions) super.setSsl(ssl);
  }

  @Override
  public VertxPgPoolOptions setKeyCertOptions(KeyCertOptions options) {
    return (VertxPgPoolOptions) super.setKeyCertOptions(options);
  }

  @Override
  public VertxPgPoolOptions setKeyStoreOptions(JksOptions options) {
    return (VertxPgPoolOptions) super.setKeyStoreOptions(options);
  }

  @Override
  public VertxPgPoolOptions setPfxKeyCertOptions(PfxOptions options) {
    return (VertxPgPoolOptions) super.setPfxKeyCertOptions(options);
  }

  @Override
  public VertxPgPoolOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (VertxPgPoolOptions) super.setPemKeyCertOptions(options);
  }

  @Override
  public VertxPgPoolOptions setTrustOptions(TrustOptions options) {
    return (VertxPgPoolOptions) super.setTrustOptions(options);
  }

  @Override
  public VertxPgPoolOptions setTrustStoreOptions(JksOptions options) {
    return (VertxPgPoolOptions) super.setTrustStoreOptions(options);
  }

  @Override
  public VertxPgPoolOptions setPemTrustOptions(PemTrustOptions options) {
    return (VertxPgPoolOptions) super.setPemTrustOptions(options);
  }

  @Override
  public VertxPgPoolOptions setPfxTrustOptions(PfxOptions options) {
    return (VertxPgPoolOptions) super.setPfxTrustOptions(options);
  }

  @Override
  public VertxPgPoolOptions addEnabledCipherSuite(String suite) {
    return (VertxPgPoolOptions) super.addEnabledCipherSuite(suite);
  }

  @Override
  public VertxPgPoolOptions addEnabledSecureTransportProtocol(String protocol) {
    return (VertxPgPoolOptions) super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public VertxPgPoolOptions addCrlPath(String crlPath) throws NullPointerException {
    return (VertxPgPoolOptions) super.addCrlPath(crlPath);
  }

  @Override
  public VertxPgPoolOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (VertxPgPoolOptions) super.addCrlValue(crlValue);
  }

  @Override
  public VertxPgPoolOptions setTrustAll(boolean trustAll) {
    return (VertxPgPoolOptions) super.setTrustAll(trustAll);
  }

  @Override
  public VertxPgPoolOptions setConnectTimeout(int connectTimeout) {
    return (VertxPgPoolOptions) super.setConnectTimeout(connectTimeout);
  }

  @Override
  public VertxPgPoolOptions setMetricsName(String metricsName) {
    return (VertxPgPoolOptions) super.setMetricsName(metricsName);
  }

  @Override
  public VertxPgPoolOptions setReconnectAttempts(int attempts) {
    return (VertxPgPoolOptions) super.setReconnectAttempts(attempts);
  }

  @Override
  public VertxPgPoolOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (VertxPgPoolOptions) super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public VertxPgPoolOptions setLogActivity(boolean logEnabled) {
    return (VertxPgPoolOptions) super.setLogActivity(logEnabled);
  }

  @Override
  public VertxPgPoolOptions setReconnectInterval(long interval) {
    return (VertxPgPoolOptions) super.setReconnectInterval(interval);
  }

  @Override
  public VertxPgPoolOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (VertxPgPoolOptions) super.setProxyOptions(proxyOptions);
  }

  @Override
  public VertxPgPoolOptions setLocalAddress(String localAddress) {
    return (VertxPgPoolOptions) super.setLocalAddress(localAddress);
  }

  @Override
  public VertxPgPoolOptions setUseAlpn(boolean useAlpn) {
    return (VertxPgPoolOptions) super.setUseAlpn(useAlpn);
  }

  @Override
  public VertxPgPoolOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (VertxPgPoolOptions) super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public VertxPgPoolOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (VertxPgPoolOptions) super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public VertxPgPoolOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (VertxPgPoolOptions) super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public VertxPgPoolOptions setReusePort(boolean reusePort) {
    return (VertxPgPoolOptions) super.setReusePort(reusePort);
  }

  @Override
  public VertxPgPoolOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (VertxPgPoolOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public VertxPgPoolOptions setTcpCork(boolean tcpCork) {
    return (VertxPgPoolOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public VertxPgPoolOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (VertxPgPoolOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    VertxPgPoolOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VertxPgPoolOptions)) return false;
    if (!super.equals(o)) return false;

    VertxPgPoolOptions that = (VertxPgPoolOptions) o;

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
