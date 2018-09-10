package io.reactiverse.pgclient;

import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Handler;

public interface TestContext {

  <T> T get(String var1);

  <T> T put(String var1, Object var2);

  <T> T remove(String var1);

  TestContext assertNull(Object var1);

  TestContext assertNull(Object var1, String var2);

  TestContext assertNotNull(Object var1);

  TestContext assertNotNull(Object var1, String var2);

  TestContext assertTrue(boolean var1);

  TestContext assertTrue(boolean var1, String var2);

  TestContext assertFalse(boolean var1);

  TestContext assertFalse(boolean var1, String var2);

  TestContext assertEquals(Object var1, Object var2);

  TestContext assertEquals(Object var1, Object var2, String var3);

  TestContext assertInRange(double var1, double var3, double var5);

  TestContext assertInRange(double var1, double var3, double var5, String var7);

  TestContext assertNotEquals(Object var1, Object var2);

  TestContext assertNotEquals(Object var1, Object var2, String var3);

  TestContext verify(Handler<Void> var1);

  void fail();

  void fail(String var1);

  void fail(Throwable var1);

  Async async();

  Async async(int var1);

  Async strictAsync(int var1);

  <T> Handler<AsyncResult<T>> asyncAssertSuccess();

  <T> Handler<AsyncResult<T>> asyncAssertSuccess(Handler<T> var1);

  <T> Handler<AsyncResult<T>> asyncAssertFailure();

  <T> Handler<AsyncResult<T>> asyncAssertFailure(Handler<Throwable> var1);

  Handler<Throwable> exceptionHandler();
}
