package io.reactiverse.pgclient.shared;

public interface ReadStream<T> {
    ReadStream<T> exceptionHandler(Handler<Throwable> var1);

    ReadStream<T> handler(Handler<T> var1);

    ReadStream<T> pause();

    ReadStream<T> resume();

    ReadStream<T> endHandler(Handler<Void> var1);
}
