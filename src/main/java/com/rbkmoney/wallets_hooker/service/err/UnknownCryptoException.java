package com.rbkmoney.wallets_hooker.service.err;

/**
 * Created by inalarsanukaev on 14.12.16.
 */
public class UnknownCryptoException extends RuntimeException {
    public UnknownCryptoException(Throwable e) {
        super(e);
    }
}
