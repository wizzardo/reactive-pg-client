package io.reactiverse.pgclient;

import io.reactiverse.pgclient.shared.AsyncResult;
import io.reactiverse.pgclient.shared.Future;
import io.reactiverse.pgclient.shared.Handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface Completion<T> {
  void resolve(Future<T> var1);

  boolean isCompleted();

  boolean isSucceeded();

  boolean isFailed();

  void handler(Handler<AsyncResult<T>> var1);

  void await();

  void await(long var1);

  void awaitSuccess();

  void awaitSuccess(long var1);

  class CompletionImpl<T> implements Completion<T> {
    protected final CompletableFuture<T> completable = new CompletableFuture<>();

    public CompletionImpl() {
    }

    public void resolve(Future future) {
      this.completable.whenComplete((done, err) -> {
        if (err != null) {
          future.fail(err);
        } else {
          future.complete();
        }

      });
    }

    public boolean isCompleted() {
      return this.completable.isDone();
    }

    public boolean isSucceeded() {
      return this.isCompleted() && !this.isFailed();
    }

    public boolean isFailed() {
      return this.completable.isCompletedExceptionally();
    }

    public void handler(Handler<AsyncResult<T>> completionHandler) {
      Future<T> completion = Future.future();
      completion.setHandler(completionHandler);
      this.resolve(completion);
    }

    public void await() {
      try {
        this.completable.get();
      } catch (ExecutionException var2) {
        ;
      } catch (InterruptedException var3) {
        Thread.currentThread().interrupt();
        uncheckedThrow(var3);
      }

    }

    public void await(long timeoutMillis) {
      try {
        this.completable.get(timeoutMillis, TimeUnit.MILLISECONDS);
      } catch (ExecutionException var4) {
        ;
      } catch (InterruptedException var5) {
        Thread.currentThread().interrupt();
        uncheckedThrow(var5);
      } catch (TimeoutException var6) {
        uncheckedThrow(new TimeoutException("Timed out"));
      }

    }

    public void awaitSuccess() {
      try {
        this.completable.get();
      } catch (ExecutionException var2) {
        uncheckedThrow(var2.getCause());
      } catch (InterruptedException var3) {
        Thread.currentThread().interrupt();
        uncheckedThrow(var3);
      }

    }

    public void awaitSuccess(long timeoutMillis) {
      try {
        this.completable.get(timeoutMillis, TimeUnit.MILLISECONDS);
      } catch (ExecutionException var4) {
        uncheckedThrow(var4.getCause());
      } catch (InterruptedException var5) {
        Thread.currentThread().interrupt();
        uncheckedThrow(var5);
      } catch (TimeoutException var6) {
        uncheckedThrow(new TimeoutException("Timed out"));
      }

    }

    public static void uncheckedThrow(Throwable throwable) {
      throwsUnchecked(throwable);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> void throwsUnchecked(Throwable toThrow) throws T {
      throw (T) toThrow;
    }
  }
}
