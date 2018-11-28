package com.rbkmoney.wallets_hooker.handler;

import com.rbkmoney.geck.filter.Filter;

public interface Handler<C, P> {
    default boolean accept(C change) {
        return getFilter().match(change);
    }
    void handle(C change, P parent);
    Filter getFilter();
}
