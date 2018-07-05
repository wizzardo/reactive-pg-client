package io.reactiverse.pgclient.shared;

public interface Future<T> extends AsyncResult<T>, Handler<AsyncResult<T>> {

    static <T> Future<T> future(Handler<Future<T>> handler) {
        Future<T> fut = future();
        handler.handle(fut);
        return fut;
    }

    static <T> Future<T> future() {
        throw new IllegalStateException("Not implemented yet");
    }

    static <T> Future<T> succeededFuture() {
        throw new IllegalStateException("Not implemented yet");
    }

    static <T> Future<T> succeededFuture(T result) {
        throw new IllegalStateException("Not implemented yet");
    }

    static <T> Future<T> failedFuture(Throwable t) {
        throw new IllegalStateException("Not implemented yet");
    }

    static <T> Future<T> failedFuture(String failureMessage) {
        throw new IllegalStateException("Not implemented yet");
    }


    void complete(T var1);

    void complete();

    void fail(Throwable var1);

    void fail(String var1);

    boolean tryComplete(T var1);

    boolean tryComplete();

    boolean tryFail(Throwable var1);

    boolean tryFail(String var1);

    boolean isComplete();

    Future<T> setHandler(Handler<AsyncResult<T>> holder);
}
