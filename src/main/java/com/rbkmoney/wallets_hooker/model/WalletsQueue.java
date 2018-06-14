package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class WalletsQueue extends Queue {
    private String walletId;

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalletsQueue)) return false;
        if (!super.equals(o)) return false;
        WalletsQueue that = (WalletsQueue) o;
        return Objects.equals(getWalletId(), that.getWalletId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getWalletId());
    }
}
