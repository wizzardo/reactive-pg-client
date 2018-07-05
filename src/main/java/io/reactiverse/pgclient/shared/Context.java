package io.reactiverse.pgclient.shared;

public interface Context {
    void runOnContext(Handler<Void> h);
}
