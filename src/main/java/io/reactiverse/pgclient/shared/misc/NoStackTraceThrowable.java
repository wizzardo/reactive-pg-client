package io.reactiverse.pgclient.shared.misc;

public class NoStackTraceThrowable extends Throwable {
    public NoStackTraceThrowable(String message) {
        super(message, (Throwable)null, false, false);
    }
}
