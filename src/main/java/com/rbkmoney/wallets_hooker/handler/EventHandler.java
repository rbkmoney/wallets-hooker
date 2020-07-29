package com.rbkmoney.wallets_hooker.handler;

public interface EventHandler<C, P> {

    boolean accept(C change);

    void handle(C change, P parent);
}
