package io.reactiverse.pgclient;

import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.AsyncResultVertxConverter;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReactiverseUnitRunner extends VertxUnitRunner {
  public ReactiverseUnitRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void invokeTestMethod(FrameworkMethod fMethod, Object test, io.vertx.ext.unit.TestContext context) throws InvocationTargetException, IllegalAccessException {
    Method method = fMethod.getMethod();
    Class<?>[] paramTypes = method.getParameterTypes();
    if (paramTypes.length == 0) {
      method.invoke(test);
    } else {
      method.invoke(test, convert(context));
    }
  }

  protected void validateTestMethod(FrameworkMethod fMethod) throws Exception {
    Class<?>[] paramTypes = fMethod.getMethod().getParameterTypes();
    if (paramTypes.length != 0 && (paramTypes.length != 1 || !paramTypes[0].equals(TestContext.class))) {
      throw new Exception("Method " + fMethod.getName() + " should have no parameters or the " + TestContext.class.getName() + " parameter");
    }
  }

  protected TestContext convert(io.vertx.ext.unit.TestContext context) {
    return new TestContext() {
      @Override
      public <T> T get(String var1) {
        return context.get(var1);
      }

      @Override
      public <T> T put(String var1, Object var2) {
        return context.put(var1, var2);
      }

      @Override
      public <T> T remove(String var1) {
        return context.remove(var1);
      }

      @Override
      public TestContext assertNull(Object var1) {
        context.assertNull(var1);
        return this;
      }

      @Override
      public TestContext assertNull(Object var1, String var2) {
        context.assertNull(var1, var2);
        return this;
      }

      @Override
      public TestContext assertNotNull(Object var1) {
        context.assertNotNull(var1);
        return this;
      }

      @Override
      public TestContext assertNotNull(Object var1, String var2) {
        context.assertNotNull(var1, var2);
        return this;
      }

      @Override
      public TestContext assertTrue(boolean var1) {
        context.assertTrue(var1);
        return this;
      }

      @Override
      public TestContext assertTrue(boolean var1, String var2) {
        context.assertTrue(var1, var2);
        return this;
      }

      @Override
      public TestContext assertFalse(boolean var1) {
        context.assertFalse(var1);
        return this;
      }

      @Override
      public TestContext assertFalse(boolean var1, String var2) {
        context.assertFalse(var1, var2);
        return this;
      }

      @Override
      public TestContext assertEquals(Object var1, Object var2) {
        context.assertEquals(var1, var2);
        return this;
      }

      @Override
      public TestContext assertEquals(Object var1, Object var2, String var3) {
        context.assertEquals(var1, var2, var3);
        return this;
      }

      @Override
      public TestContext assertInRange(double var1, double var3, double var5) {
        context.assertInRange(var1, var3, var5);
        return this;
      }

      @Override
      public TestContext assertInRange(double var1, double var3, double var5, String var7) {
        context.assertInRange(var1, var3, var5, var7);
        return this;
      }

      @Override
      public TestContext assertNotEquals(Object var1, Object var2) {
        context.assertNotEquals(var1, var2);
        return this;
      }

      @Override
      public TestContext assertNotEquals(Object var1, Object var2, String var3) {
        context.assertNotEquals(var1, var2, var3);
        return this;
      }

      @Override
      public TestContext verify(Handler<Void> var1) {
        context.verify(var1::handle);
        return this;
      }

      @Override
      public void fail() {
        context.fail();
      }

      @Override
      public void fail(String var1) {
        context.fail(var1);
      }

      @Override
      public void fail(Throwable var1) {
        context.fail(var1);
      }

      @Override
      public Async async() {
        return convert(context.async());
      }

      @Override
      public Async async(int var1) {
        return convert(context.async(var1));
      }

      @Override
      public Async strictAsync(int var1) {
        return convert(context.strictAsync(var1));
      }

      @Override
      public <T> Handler<AsyncResult<T>> asyncAssertSuccess() {
        return AsyncResultVertxConverter.from(context.asyncAssertSuccess());
      }

      @Override
      public <T> Handler<AsyncResult<T>> asyncAssertSuccess(Handler<T> var1) {
        return AsyncResultVertxConverter.from(context.asyncAssertSuccess(var1::handle));
      }

      @Override
      public <T> Handler<AsyncResult<T>> asyncAssertFailure() {
        return AsyncResultVertxConverter.from(context.asyncAssertFailure());
      }

      @Override
      public <T> Handler<AsyncResult<T>> asyncAssertFailure(Handler<Throwable> var1) {
        return AsyncResultVertxConverter.from(context.asyncAssertFailure(var1::handle));
      }

      @Override
      public Handler<Throwable> exceptionHandler() {
        return throwable -> context.exceptionHandler().handle(throwable);
      }
    };
  }

  static Async convert(io.vertx.ext.unit.Async async) {
    return new Async() {
      @Override
      public int count() {
        return async.count();
      }

      @Override
      public void countDown() {
        async.countDown();
      }

      @Override
      public void complete() {
        async.complete();
      }

      @Override
      public void resolve(Future<Void> var1) {
        async.resolve(convert(var1));
      }

      @Override
      public boolean isCompleted() {
        return async.isCompleted();
      }

      @Override
      public boolean isSucceeded() {
        return async.isSucceeded();
      }

      @Override
      public boolean isFailed() {
        return async.isFailed();
      }

      @Override
      public void handler(Handler<AsyncResult<Void>> var1) {
        async.handler(ar -> var1.handle(AsyncResultVertxConverter.from(ar)));
      }

      @Override
      public void await() {
        async.await();
      }

      @Override
      public void await(long var1) {
        async.await(var1);
      }

      @Override
      public void awaitSuccess() {
        async.awaitSuccess();
      }

      @Override
      public void awaitSuccess(long var1) {
        async.awaitSuccess(var1);
      }
    };
  }

  static <T> io.vertx.core.Future<T> convert(Future<T> future) {
    return new io.vertx.core.Future<T>() {
      @Override
      public boolean isComplete() {
        return future.isComplete();
      }

      @Override
      public io.vertx.core.Future<T> setHandler(io.vertx.core.Handler<io.vertx.core.AsyncResult<T>> handler) {
        future.setHandler(asyncResult -> handler.handle(AsyncResultVertxConverter.to(asyncResult)));
        return this;
      }

      @Override
      public void complete(T o) {
        future.complete(o);
      }

      @Override
      public void complete() {
        future.complete();
      }

      @Override
      public void fail(Throwable throwable) {
        future.fail(throwable);
      }

      @Override
      public void fail(String s) {
        future.fail(s);
      }

      @Override
      public boolean tryComplete(T o) {
        return future.tryComplete(o);
      }

      @Override
      public boolean tryComplete() {
        return future.tryComplete();
      }

      @Override
      public boolean tryFail(Throwable throwable) {
        return future.tryFail(throwable);
      }

      @Override
      public boolean tryFail(String s) {
        return future.tryFail(s);
      }

      @Override
      public T result() {
        return future.result();
      }

      @Override
      public Throwable cause() {
        return future.cause();
      }

      @Override
      public boolean succeeded() {
        return future.succeeded();
      }

      @Override
      public boolean failed() {
        return future.failed();
      }

      @Override
      public void handle(io.vertx.core.AsyncResult<T> asyncResult) {
        future.handle(AsyncResultVertxConverter.from(asyncResult));
      }
    };
  }
}
