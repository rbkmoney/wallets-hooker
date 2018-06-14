package com.rbkmoney.wallets_hooker.model;


import java.util.Objects;


/**
 * Created by inalarsanukaev on 07.04.17.
 */
public class WalletsMessage extends Message {
    private EventType eventType;
    private String partyId;
    private String walletId;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalletsMessage)) return false;
        WalletsMessage that = (WalletsMessage) o;
        return getEventType() == that.getEventType() &&
                Objects.equals(getPartyId(), that.getPartyId()) &&
                Objects.equals(getWalletId(), that.getWalletId()) &&
                Objects.equals(getEvent(), that.getEvent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventType(), getPartyId(), getWalletId(), getEvent());
    }

    @Override
    public String toString() {
        return "WalletsMessage{" +
                "eventType=" + eventType +
                ", partyId='" + partyId + '\'' +
                ", walletId='" + walletId + '\'' +
                ", event=" + getEvent() +
                "} " + super.toString();
    }
}
