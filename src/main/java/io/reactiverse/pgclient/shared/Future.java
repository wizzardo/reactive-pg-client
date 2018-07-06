package io.reactiverse.pgclient.shared;

import io.vertx.core.impl.NoStackTraceThrowable;

public interface Future<T> extends AsyncResult<T>, Handler<AsyncResult<T>> {

    Future EMPTY_SUCCEEDED = succeededFuture(null);

    static <T> Future<T> future(Handler<Future<T>> handler) {
        Future<T> fut = future();
        handler.handle(fut);
        return fut;
    }

    static <T> Future<T> future() {
        return new FutureImpl<>();
    }

    static <T> Future<T> succeededFuture() {
        return EMPTY_SUCCEEDED;
    }

    static <T> Future<T> succeededFuture(T result) {
        return new SucceededFuture<>(result);
    }

    static <T> Future<T> failedFuture(Throwable t) {
        return new FailedFuture<T>(t);
    }

    static <T> Future<T> failedFuture(String failureMessage) {
        return new FailedFuture<T>(failureMessage);
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


    class SucceededFuture<T> implements Future<T> {
        private final T result;

        SucceededFuture(T result) {
            this.result = result;
        }

        public boolean isComplete() {
            return true;
        }

        public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
            handler.handle(this);
            return this;
        }

        public void complete(T result) {
            throw new IllegalStateException("Result is already complete: succeeded");
        }

        public void complete() {
            throw new IllegalStateException("Result is already complete: succeeded");
        }

        public void fail(Throwable cause) {
            throw new IllegalStateException("Result is already complete: succeeded");
        }

        public void fail(String failureMessage) {
            throw new IllegalStateException("Result is already complete: succeeded");
        }

        public boolean tryComplete(T result) {
            return false;
        }

        public boolean tryComplete() {
            return false;
        }

        public boolean tryFail(Throwable cause) {
            return false;
        }

        public boolean tryFail(String failureMessage) {
            return false;
        }

        public T result() {
            return this.result;
        }

        public Throwable cause() {
            return null;
        }

        public boolean succeeded() {
            return true;
        }

        public boolean failed() {
            return false;
        }

        public void handle(AsyncResult<T> asyncResult) {
            throw new IllegalStateException("Result is already complete: succeeded");
        }

        public String toString() {
            return "Future{result=" + this.result + "}";
        }
    }

    class FailedFuture<T> implements Future<T> {
        private final Throwable cause;

        FailedFuture(Throwable t) {
            this.cause = (Throwable) (t != null ? t : new NoStackTraceThrowable((String) null));
        }

        FailedFuture(String failureMessage) {
            this((Throwable) (new NoStackTraceThrowable(failureMessage)));
        }

        public boolean isComplete() {
            return true;
        }

        public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
            handler.handle(this);
            return this;
        }

        public void complete(T result) {
            throw new IllegalStateException("Result is already complete: failed");
        }

        public void complete() {
            throw new IllegalStateException("Result is already complete: failed");
        }

        public void fail(Throwable cause) {
            throw new IllegalStateException("Result is already complete: failed");
        }

        public void fail(String failureMessage) {
            throw new IllegalStateException("Result is already complete: failed");
        }

        public boolean tryComplete(T result) {
            return false;
        }

        public boolean tryComplete() {
            return false;
        }

        public boolean tryFail(Throwable cause) {
            return false;
        }

        public boolean tryFail(String failureMessage) {
            return false;
        }

        public T result() {
            return null;
        }

        public Throwable cause() {
            return this.cause;
        }

        public boolean succeeded() {
            return false;
        }

        public boolean failed() {
            return true;
        }

        public void handle(AsyncResult<T> asyncResult) {
            throw new IllegalStateException("Result is already complete: failed");
        }

        public String toString() {
            return "Future{cause=" + this.cause.getMessage() + "}";
        }
    }

    class FutureImpl<T> implements Future<T>, Handler<AsyncResult<T>> {
        private boolean failed;
        private boolean succeeded;
        private Handler<AsyncResult<T>> handler;
        private T result;
        private Throwable throwable;

        FutureImpl() {
        }

        public T result() {
            return this.result;
        }

        public Throwable cause() {
            return this.throwable;
        }

        public synchronized boolean succeeded() {
            return this.succeeded;
        }

        public synchronized boolean failed() {
            return this.failed;
        }

        public synchronized boolean isComplete() {
            return this.failed || this.succeeded;
        }

        public Future<T> setHandler(Handler<AsyncResult<T>> handler) {
            boolean callHandler;
            synchronized (this) {
                this.handler = handler;
                callHandler = this.isComplete();
            }

            if (callHandler) {
                handler.handle(this);
            }

            return this;
        }

        public void complete(T result) {
            if (!this.tryComplete(result)) {
                throw new IllegalStateException("Result is already complete: " + (this.succeeded ? "succeeded" : "failed"));
            }
        }

        public void complete() {
            if (!this.tryComplete()) {
                throw new IllegalStateException("Result is already complete: " + (this.succeeded ? "succeeded" : "failed"));
            }
        }

        public void fail(Throwable cause) {
            if (!this.tryFail(cause)) {
                throw new IllegalStateException("Result is already complete: " + (this.succeeded ? "succeeded" : "failed"));
            }
        }

        public void fail(String failureMessage) {
            if (!this.tryFail(failureMessage)) {
                throw new IllegalStateException("Result is already complete: " + (this.succeeded ? "succeeded" : "failed"));
            }
        }

        public boolean tryComplete(T result) {
            Handler h;
            synchronized (this) {
                if (this.succeeded || this.failed) {
                    return false;
                }

                this.result = result;
                this.succeeded = true;
                h = this.handler;
            }

            if (h != null) {
                h.handle(this);
            }

            return true;
        }

        public boolean tryComplete() {
            return this.tryComplete((T) null);
        }

        public void handle(Future<T> ar) {
            if (ar.succeeded()) {
                this.complete(ar.result());
            } else {
                this.fail(ar.cause());
            }

        }

        public Handler<AsyncResult<T>> completer() {
            return this;
        }

        public void handle(AsyncResult<T> asyncResult) {
            if (asyncResult.succeeded()) {
                this.complete(asyncResult.result());
            } else {
                this.fail(asyncResult.cause());
            }

        }

        public boolean tryFail(Throwable cause) {
            Handler h;
            synchronized (this) {
                if (this.succeeded || this.failed) {
                    return false;
                }

                this.throwable = (Throwable) (cause != null ? cause : new NoStackTraceThrowable((String) null));
                this.failed = true;
                h = this.handler;
            }

            if (h != null) {
                h.handle(this);
            }

            return true;
        }

        public boolean tryFail(String failureMessage) {
            return this.tryFail((Throwable) (new NoStackTraceThrowable(failureMessage)));
        }

        public String toString() {
            synchronized (this) {
                if (this.succeeded) {
                    return "Future{result=" + this.result + "}";
                } else {
                    return this.failed ? "Future{cause=" + this.throwable.getMessage() + "}" : "Future{unresolved}";
                }
            }
        }
    }

}
