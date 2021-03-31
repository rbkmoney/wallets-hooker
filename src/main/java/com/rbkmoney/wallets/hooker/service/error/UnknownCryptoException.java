package com.rbkmoney.wallets.hooker.service.error;

/**
 * Created by inalarsanukaev on 14.12.16.
 */
public class UnknownCryptoException extends RuntimeException {
    public UnknownCryptoException(Throwable e) {
        super(e);
    }
}
