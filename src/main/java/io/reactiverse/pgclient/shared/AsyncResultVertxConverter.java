package io.reactiverse.pgclient.shared;

public interface AsyncResultVertxConverter {
    static <T> AsyncResult<T> from(io.vertx.core.AsyncResult<T> ar) {
        return new AsyncResult<T>() {
            @Override
            public T result() {
                return ar.result();
            }

            @Override
            public Throwable cause() {
                return ar.cause();
            }

            @Override
            public boolean succeeded() {
                return ar.succeeded();
            }

            @Override
            public boolean failed() {
                return ar.failed();
            }
        };
    }

    static <T> io.vertx.core.AsyncResult<T> to(AsyncResult<T> ar) {
        return new io.vertx.core.AsyncResult<T>() {
            @Override
            public T result() {
                return ar.result();
            }

            @Override
            public Throwable cause() {
                return ar.cause();
            }

            @Override
            public boolean succeeded() {
                return ar.succeeded();
            }

            @Override
            public boolean failed() {
                return ar.failed();
            }
        };
    }

    static <T> Handler<AsyncResult<T>> from(io.vertx.core.Handler<io.vertx.core.AsyncResult<T>> handler) {
        return ar -> handler.handle(to(ar));
    }
}
