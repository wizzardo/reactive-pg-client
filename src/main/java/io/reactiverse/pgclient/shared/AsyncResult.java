package io.reactiverse.pgclient.shared;

import java.util.function.Function;

public interface AsyncResult<T> {
    T result();

    Throwable cause();

    boolean succeeded();

    boolean failed();


    default <V> AsyncResult<V> map(V value) {
        return this.map((t) -> value);
    }

    default <U> AsyncResult<U> map(final Function<T, U> mapper) {
        if (mapper == null) {
            throw new NullPointerException();
        } else {
            return new AsyncResult<U>() {
                public U result() {
                    return this.succeeded() ? mapper.apply(AsyncResult.this.result()) : null;
                }

                public Throwable cause() {
                    return AsyncResult.this.cause();
                }

                public boolean succeeded() {
                    return AsyncResult.this.succeeded();
                }

                public boolean failed() {
                    return AsyncResult.this.failed();
                }
            };
        }
    }

    default <V> AsyncResult<V> mapEmpty() {
        return this.map(null);
    }
}
