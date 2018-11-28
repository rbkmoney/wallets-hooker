package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

public class WithdrawalQueue extends Queue {
    private String withdrawalId;

    public String getWithdrawalId() {
        return withdrawalId;
    }

    public void setWithdrawalId(String withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WithdrawalQueue)) return false;
        if (!super.equals(o)) return false;
        WithdrawalQueue that = (WithdrawalQueue) o;
        return Objects.equals(getWithdrawalId(), that.getWithdrawalId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getWithdrawalId());
    }
}
