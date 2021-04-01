package com.rbkmoney.wallets.hooker.exception;

public class GenerateMessageException extends RuntimeException {
    public GenerateMessageException() {
    }

    public GenerateMessageException(String message) {
        super(message);
    }

    public GenerateMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public GenerateMessageException(Throwable cause) {
        super(cause);
    }

    public GenerateMessageException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
