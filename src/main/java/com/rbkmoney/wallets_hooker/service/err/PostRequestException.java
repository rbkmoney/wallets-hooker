package com.rbkmoney.wallets_hooker.service.err;

public class PostRequestException extends Exception {
    public PostRequestException(Throwable cause) {
        super(cause);
    }

    public PostRequestException(String errMessage) {
        super(errMessage);
    }

    @Override
    public String getMessage() {
        String message = getCause() != null ? getCause().getMessage() : super.getMessage();
        return "Unknown error during request to merchant execution. \n" + message;
    }
}
