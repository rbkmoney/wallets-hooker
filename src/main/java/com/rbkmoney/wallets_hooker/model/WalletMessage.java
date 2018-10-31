package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

public class WalletMessage extends Message {
    private String walletId;
    private String identityId;

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalletMessage)) return false;
        if (!super.equals(o)) return false;
        WalletMessage that = (WalletMessage) o;
        return Objects.equals(getWalletId(), that.getWalletId()) &&
                Objects.equals(getIdentityId(), that.getIdentityId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getWalletId(), getIdentityId());
    }

    @Override
    public String toString() {
        return "WalletMessage{" +
                "walletId='" + walletId + '\'' +
                ", identityId='" + identityId + '\'' +
                "} " + super.toString();
    }
}
