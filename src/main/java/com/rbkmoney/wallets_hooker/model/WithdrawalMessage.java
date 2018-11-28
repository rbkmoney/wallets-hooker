package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

public class WithdrawalMessage extends Message {
    private String withdrawalId;
    private String withdrawalCreatedAt;
    private String withdrawalWalletId;
    private String withdrawalDestinationId;
    private Long withdrawalAmount;
    private String withdrawalCurrencyCode;
    private String withdrawalMetadata;
    private String withdrawalStatus;
    private String withdrawalFailureCode;

    public String getWithdrawalId() {
        return withdrawalId;
    }

    public void setWithdrawalId(String withdrawalId) {
        this.withdrawalId = withdrawalId;
    }

    public String getWithdrawalCreatedAt() {
        return withdrawalCreatedAt;
    }

    public void setWithdrawalCreatedAt(String withdrawalCreatedAt) {
        this.withdrawalCreatedAt = withdrawalCreatedAt;
    }

    public String getWithdrawalWalletId() {
        return withdrawalWalletId;
    }

    public void setWithdrawalWalletId(String withdrawalWalletId) {
        this.withdrawalWalletId = withdrawalWalletId;
    }

    public String getWithdrawalDestinationId() {
        return withdrawalDestinationId;
    }

    public void setWithdrawalDestinationId(String withdrawalDestinationId) {
        this.withdrawalDestinationId = withdrawalDestinationId;
    }

    public Long getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(Long withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public String getWithdrawalCurrencyCode() {
        return withdrawalCurrencyCode;
    }

    public void setWithdrawalCurrencyCode(String withdrawalCurrencyCode) {
        this.withdrawalCurrencyCode = withdrawalCurrencyCode;
    }

    public String getWithdrawalMetadata() {
        return withdrawalMetadata;
    }

    public void setWithdrawalMetadata(String withdrawalMetadata) {
        this.withdrawalMetadata = withdrawalMetadata;
    }

    public String getWithdrawalStatus() {
        return withdrawalStatus;
    }

    public void setWithdrawalStatus(String withdrawalStatus) {
        this.withdrawalStatus = withdrawalStatus;
    }

    public String getWithdrawalFailureCode() {
        return withdrawalFailureCode;
    }

    public void setWithdrawalFailureCode(String withdrawalFailureCode) {
        this.withdrawalFailureCode = withdrawalFailureCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WithdrawalMessage)) return false;
        if (!super.equals(o)) return false;
        WithdrawalMessage that = (WithdrawalMessage) o;
        return Objects.equals(getWithdrawalId(), that.getWithdrawalId()) &&
                Objects.equals(getWithdrawalCreatedAt(), that.getWithdrawalCreatedAt()) &&
                Objects.equals(getWithdrawalWalletId(), that.getWithdrawalWalletId()) &&
                Objects.equals(getWithdrawalDestinationId(), that.getWithdrawalDestinationId()) &&
                Objects.equals(getWithdrawalAmount(), that.getWithdrawalAmount()) &&
                Objects.equals(getWithdrawalCurrencyCode(), that.getWithdrawalCurrencyCode()) &&
                Objects.equals(getWithdrawalMetadata(), that.getWithdrawalMetadata()) &&
                Objects.equals(getWithdrawalStatus(), that.getWithdrawalStatus()) &&
                Objects.equals(getWithdrawalFailureCode(), that.getWithdrawalFailureCode());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getWithdrawalId(), getWithdrawalCreatedAt(), getWithdrawalWalletId(), getWithdrawalDestinationId(), getWithdrawalAmount(), getWithdrawalCurrencyCode(), getWithdrawalMetadata(), getWithdrawalStatus(), getWithdrawalFailureCode());
    }

    @Override
    public String toString() {
        return "WithdrawalMessage{" +
                "withdrawalId='" + withdrawalId + '\'' +
                ", withdrawalCreatedAt='" + withdrawalCreatedAt + '\'' +
                ", withdrawalWalletId='" + withdrawalWalletId + '\'' +
                ", withdrawalDestinationId='" + withdrawalDestinationId + '\'' +
                ", withdrawalAmount=" + withdrawalAmount +
                ", withdrawalCurrencyCode='" + withdrawalCurrencyCode + '\'' +
                ", withdrawalMetadata='" + withdrawalMetadata + '\'' +
                ", withdrawalStatus='" + withdrawalStatus + '\'' +
                ", withdrawalFailureCode='" + withdrawalFailureCode + '\'' +
                "} " + super.toString();
    }
}
