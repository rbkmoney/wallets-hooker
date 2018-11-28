package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

public class IdentityMessage extends Message {
    private String identityId;

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityMessage)) return false;
        if (!super.equals(o)) return false;
        IdentityMessage that = (IdentityMessage) o;
        return Objects.equals(getIdentityId(), that.getIdentityId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getIdentityId());
    }

    @Override
    public String toString() {
        return "IdentityMessage{" +
                "identityId='" + identityId + '\'' +
                "} " + super.toString();
    }
}
