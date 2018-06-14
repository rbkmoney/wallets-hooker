package com.rbkmoney.wallets_hooker.model;

public enum EventType {
    WALLET_WITHDRAWAL_CREATED("wallet_withdrawal_created"),
    WALLET_WITHDRAWAL_SUCCEEDED("wallet_withdrawal_succeeded"),
    WALLET_WITHDRAWAL_FAILED("wallet_withdrawal_failed");

    private String thriftFilterPathCoditionRule;

    EventType(String thriftFilterPathCoditionRule) {
        this.thriftFilterPathCoditionRule = thriftFilterPathCoditionRule;
    }

    public String getThriftFilterPathCoditionRule() {
        return thriftFilterPathCoditionRule;
    }
}
