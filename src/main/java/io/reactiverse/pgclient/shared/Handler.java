package io.reactiverse.pgclient.shared;

@FunctionalInterface
public interface Handler<E> {
    void handle(E e);
}

