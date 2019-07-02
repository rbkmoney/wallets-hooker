package com.rbkmoney.wallets_hooker.exception;

public class HandleEventException extends RuntimeException {
    public HandleEventException() {
    }

    public HandleEventException(String message) {
        super(message);
    }

    public HandleEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandleEventException(Throwable cause) {
        super(cause);
    }

    public HandleEventException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
