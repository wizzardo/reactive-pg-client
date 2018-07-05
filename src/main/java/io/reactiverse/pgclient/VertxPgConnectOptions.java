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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.*;

import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author Billy Yuan <billy112487983@gmail.com>
 */
@DataObject(generateConverter = true)
public class VertxPgConnectOptions extends NetClientOptions implements PgConnectOptions {

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

  public VertxPgConnectOptions() {
    super();
    init();
  }

  public VertxPgConnectOptions(JsonObject json) {
    super(json);
    init();
    VertxPgConnectOptionsConverter.fromJson(json, this);
  }

  public VertxPgConnectOptions(VertxPgConnectOptions other) {
    super(other);
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
  public VertxPgConnectOptions setHost(String host) {
    this.host = host;
    return this;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public VertxPgConnectOptions setPort(int port) {
    this.port = port;
    return this;
  }

  @Override
  public String getDatabase() {
    return database;
  }

  @Override
  public VertxPgConnectOptions setDatabase(String database) {
    this.database = database;
    return this;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public VertxPgConnectOptions setUser(String user) {
    this.user = user;
    return this;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public VertxPgConnectOptions setPassword(String password) {
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

  @Override
  public VertxPgConnectOptions setSendBufferSize(int sendBufferSize) {
    return (VertxPgConnectOptions)super.setSendBufferSize(sendBufferSize);
  }

  @Override
  public VertxPgConnectOptions setReceiveBufferSize(int receiveBufferSize) {
    return (VertxPgConnectOptions)super.setReceiveBufferSize(receiveBufferSize);
  }

  @Override
  public VertxPgConnectOptions setReuseAddress(boolean reuseAddress) {
    return (VertxPgConnectOptions)super.setReuseAddress(reuseAddress);
  }

  @Override
  public VertxPgConnectOptions setTrafficClass(int trafficClass) {
    return (VertxPgConnectOptions)super.setTrafficClass(trafficClass);
  }

  @Override
  public VertxPgConnectOptions setTcpNoDelay(boolean tcpNoDelay) {
    return (VertxPgConnectOptions)super.setTcpNoDelay(tcpNoDelay);
  }

  @Override
  public VertxPgConnectOptions setTcpKeepAlive(boolean tcpKeepAlive) {
    return (VertxPgConnectOptions)super.setTcpKeepAlive(tcpKeepAlive);
  }

  @Override
  public VertxPgConnectOptions setSoLinger(int soLinger) {
    return (VertxPgConnectOptions)super.setSoLinger(soLinger);
  }

  @Override
  public VertxPgConnectOptions setUsePooledBuffers(boolean usePooledBuffers) {
    return (VertxPgConnectOptions)super.setUsePooledBuffers(usePooledBuffers);
  }

  @Override
  public VertxPgConnectOptions setIdleTimeout(int idleTimeout) {
    return (VertxPgConnectOptions)super.setIdleTimeout(idleTimeout);
  }

  @Override
  public VertxPgConnectOptions setSsl(boolean ssl) {
    return (VertxPgConnectOptions)super.setSsl(ssl);
  }

  @Override
  public VertxPgConnectOptions setKeyCertOptions(KeyCertOptions options) {
    return (VertxPgConnectOptions)super.setKeyCertOptions(options);
  }

  @Override
  public VertxPgConnectOptions setKeyStoreOptions(JksOptions options) {
    return (VertxPgConnectOptions)super.setKeyStoreOptions(options);
  }

  @Override
  public VertxPgConnectOptions setPfxKeyCertOptions(PfxOptions options) {
    return (VertxPgConnectOptions)super.setPfxKeyCertOptions(options);
  }

  @Override
  public VertxPgConnectOptions setPemKeyCertOptions(PemKeyCertOptions options) {
    return (VertxPgConnectOptions)super.setPemKeyCertOptions(options);
  }

  @Override
  public VertxPgConnectOptions setTrustOptions(TrustOptions options) {
    return (VertxPgConnectOptions)super.setTrustOptions(options);
  }

  @Override
  public VertxPgConnectOptions setTrustStoreOptions(JksOptions options) {
    return (VertxPgConnectOptions)super.setTrustStoreOptions(options);
  }

  @Override
  public VertxPgConnectOptions setPemTrustOptions(PemTrustOptions options) {
    return (VertxPgConnectOptions)super.setPemTrustOptions(options);
  }

  @Override
  public VertxPgConnectOptions setPfxTrustOptions(PfxOptions options) {
    return (VertxPgConnectOptions)super.setPfxTrustOptions(options);
  }

  @Override
  public VertxPgConnectOptions addEnabledCipherSuite(String suite) {
    return (VertxPgConnectOptions)super.addEnabledCipherSuite(suite);
  }

  @Override
  public VertxPgConnectOptions addEnabledSecureTransportProtocol(String protocol) {
    return (VertxPgConnectOptions)super.addEnabledSecureTransportProtocol(protocol);
  }

  @Override
  public VertxPgConnectOptions addCrlPath(String crlPath) throws NullPointerException {
    return (VertxPgConnectOptions)super.addCrlPath(crlPath);
  }

  @Override
  public VertxPgConnectOptions addCrlValue(Buffer crlValue) throws NullPointerException {
    return (VertxPgConnectOptions)super.addCrlValue(crlValue);
  }

  @Override
  public VertxPgConnectOptions setTrustAll(boolean trustAll) {
    return (VertxPgConnectOptions)super.setTrustAll(trustAll);
  }

  @Override
  public VertxPgConnectOptions setConnectTimeout(int connectTimeout) {
    return (VertxPgConnectOptions)super.setConnectTimeout(connectTimeout);
  }

  @Override
  public VertxPgConnectOptions setMetricsName(String metricsName) {
    return (VertxPgConnectOptions)super.setMetricsName(metricsName);
  }

  @Override
  public VertxPgConnectOptions setReconnectAttempts(int attempts) {
    return (VertxPgConnectOptions)super.setReconnectAttempts(attempts);
  }

  @Override
  public VertxPgConnectOptions setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
    return (VertxPgConnectOptions)super.setHostnameVerificationAlgorithm(hostnameVerificationAlgorithm);
  }

  @Override
  public VertxPgConnectOptions setLogActivity(boolean logEnabled) {
    return (VertxPgConnectOptions)super.setLogActivity(logEnabled);
  }

  @Override
  public VertxPgConnectOptions setReconnectInterval(long interval) {
    return (VertxPgConnectOptions)super.setReconnectInterval(interval);
  }

  @Override
  public VertxPgConnectOptions setProxyOptions(ProxyOptions proxyOptions) {
    return (VertxPgConnectOptions)super.setProxyOptions(proxyOptions);
  }

  @Override
  public VertxPgConnectOptions setLocalAddress(String localAddress) {
    return (VertxPgConnectOptions)super.setLocalAddress(localAddress);
  }

  @Override
  public VertxPgConnectOptions setUseAlpn(boolean useAlpn) {
    return (VertxPgConnectOptions)super.setUseAlpn(useAlpn);
  }

  @Override
  public VertxPgConnectOptions setSslEngineOptions(SSLEngineOptions sslEngineOptions) {
    return (VertxPgConnectOptions)super.setSslEngineOptions(sslEngineOptions);
  }

  @Override
  public VertxPgConnectOptions setJdkSslEngineOptions(JdkSSLEngineOptions sslEngineOptions) {
    return (VertxPgConnectOptions)super.setJdkSslEngineOptions(sslEngineOptions);
  }

  @Override
  public VertxPgConnectOptions setOpenSslEngineOptions(OpenSSLEngineOptions sslEngineOptions) {
    return (VertxPgConnectOptions)super.setOpenSslEngineOptions(sslEngineOptions);
  }

  @Override
  public VertxPgConnectOptions setReusePort(boolean reusePort) {
    return (VertxPgConnectOptions) super.setReusePort(reusePort);
  }

  @Override
  public VertxPgConnectOptions setTcpFastOpen(boolean tcpFastOpen) {
    return (VertxPgConnectOptions) super.setTcpFastOpen(tcpFastOpen);
  }

  @Override
  public VertxPgConnectOptions setTcpCork(boolean tcpCork) {
    return (VertxPgConnectOptions) super.setTcpCork(tcpCork);
  }

  @Override
  public VertxPgConnectOptions setTcpQuickAck(boolean tcpQuickAck) {
    return (VertxPgConnectOptions) super.setTcpQuickAck(tcpQuickAck);
  }

  @Override
  public VertxPgConnectOptions setEnabledSecureTransportProtocols(Set<String> enabledSecureTransportProtocols) {
    return (VertxPgConnectOptions) super.setEnabledSecureTransportProtocols(enabledSecureTransportProtocols);
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
  public JsonObject toJson() {
    JsonObject json = super.toJson();
    VertxPgConnectOptionsConverter.toJson(this, json);
    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VertxPgConnectOptions)) return false;
    if (!super.equals(o)) return false;

    VertxPgConnectOptions that = (VertxPgConnectOptions) o;

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
