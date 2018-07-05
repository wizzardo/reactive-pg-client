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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public abstract class PgTestBase {

  private static EmbeddedPostgres postgres;
  static VertxPgConnectOptions options;

  @BeforeClass
  public static void before() throws Exception {
    options = startPg();
  }

  @AfterClass
  public static void after() throws Exception {
    stopPg();
  }

  public synchronized static VertxPgConnectOptions startPg() throws Exception {
    return startPg(false);
  }

  public synchronized static VertxPgConnectOptions startPg(boolean domainSockets) throws Exception {
    if (postgres != null) {
      throw new IllegalStateException();
    }
    IRuntimeConfig config;
    String a = System.getProperty("target.dir", "target");
    File targetDir = new File(a);
    if (targetDir.exists() && targetDir.isDirectory()) {
      config = EmbeddedPostgres.cachedRuntimeConfig(targetDir.toPath());
    } else {
      throw new AssertionError("Cannot access target dir");
    }

    // Domain sockets
    File sock;
    if (domainSockets) {
      sock = Files.createTempFile(targetDir.toPath(), "pg_", ".sock").toFile();
      assertTrue(sock.delete());
      assertTrue(sock.mkdir());
      Files.setPosixFilePermissions(sock.toPath(), new HashSet<>(Arrays.asList(
        PosixFilePermission.OWNER_EXECUTE,
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.GROUP_EXECUTE,
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.GROUP_WRITE
      )));
    } else {
      sock = null;
    }

    // SSL
    File sslKey = getResourceAsFile("tls/server.key");
    Files.setPosixFilePermissions(sslKey.toPath(), Collections.singleton(PosixFilePermission.OWNER_READ));
    File sslCrt = getResourceAsFile("tls/server.crt");

    postgres = new EmbeddedPostgres(V9_6);
    IRuntimeConfig sslConfig = new IRuntimeConfig() {
      @Override
      public ProcessOutput getProcessOutput() {
        return config.getProcessOutput();
      }
      @Override
      public ICommandLinePostProcessor getCommandLinePostProcessor() {
        ICommandLinePostProcessor commandLinePostProcessor = config.getCommandLinePostProcessor();
        return (distribution, args) -> {
          List<String> result = commandLinePostProcessor.process(distribution, args);
          if (result.get(0).endsWith("postgres")) {
            result = new ArrayList<>(result);
            result.add("--ssl=on");
            result.add("--ssl_cert_file=" + sslCrt.getAbsolutePath());
            result.add("--ssl_key_file=" + sslKey.getAbsolutePath());
            if (domainSockets) {
              result.add("--unix_socket_directories=" + sock.getAbsolutePath());
            }
          }
          return result;
        };
      }
      @Override
      public IArtifactStore getArtifactStore() {
        return config.getArtifactStore();
      }
      @Override
      public boolean isDaemonProcess() {
        return config.isDaemonProcess();
      }
    };
    PgTestBase.postgres.start(sslConfig,
      "localhost",
      8081,
      "postgres",
      "postgres",
      "postgres",
      Collections.emptyList());
    File setupFile = getResourceAsFile("create-postgres.sql");
    PgTestBase.postgres.getProcess().get().importFromFile(setupFile);
    VertxPgConnectOptions options = new VertxPgConnectOptions();
    options.setHost(domainSockets ? sock.getAbsolutePath() : "localhost");
    options.setPort(8081);
    options.setUser("postgres");
    options.setPassword("postgres");
    options.setDatabase("postgres");
    return options;
  }

  public synchronized static void stopPg() throws Exception {
    if (postgres != null) {
      try {
        postgres.stop();
      } finally {
        postgres = null;
      }
    }
  }

  private static File getResourceAsFile(String name) throws Exception {
    InputStream in = PgTestBase.class.getClassLoader().getResourceAsStream(name);
    Path path = Files.createTempFile("pg-client", ".tmp");
    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    File file = path.toFile();
    file.deleteOnExit();
    return file;
  }
}
