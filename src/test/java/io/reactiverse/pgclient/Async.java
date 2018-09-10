package io.reactiverse.pgclient;

import java.util.concurrent.atomic.AtomicInteger;

public interface Async extends Completion<Void> {
  int count();

  void countDown();

  void complete();

  class AsyncImpl extends Completion.CompletionImpl<Void> implements Async {
    private final int initialCount;
    private final AtomicInteger current;
    private final boolean strict;

    public AsyncImpl(int initialCount, boolean strict) {
      this.initialCount = initialCount;
      this.strict = strict;
      this.current = new AtomicInteger(initialCount);
    }

    public int count() {
      return this.current.get();
    }

    public void countDown() {
      int oldValue;
      int newValue;
      do {
        oldValue = this.current.get();
        if (oldValue == 0) {
          newValue = 0;
          if (this.strict) {
            String msg;
            if (this.initialCount == 1) {
              msg = "Countdown invoked more than once";
            } else if (this.initialCount == 2) {
              msg = "Countdown invoked more than twice";
            } else {
              msg = "Countdown invoked more than " + this.initialCount + " times";
            }

            throw new IllegalStateException(msg);
          }
        } else {
          newValue = oldValue - 1;
        }
      } while (!this.current.compareAndSet(oldValue, newValue));

      if (newValue == 0) {
        this.release((Throwable) null);
      }

    }

    public void complete() {
      int value = this.current.getAndSet(0);
      if (value > 0) {
        this.release((Throwable) null);
      } else {
        throw new IllegalStateException("The Async complete method has been called more than " + this.initialCount + " times, check your test.");
      }
    }

    void release(Throwable failure) {
      if (failure != null) {
        this.completable.completeExceptionally(failure);
      } else {
        this.completable.complete((Void) null);
      }

    }
  }
}
