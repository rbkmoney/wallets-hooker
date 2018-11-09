package com.rbkmoney.wallets_hooker.model;

import com.rbkmoney.wallets_hooker.retry.RetryPolicyType;

import java.util.Objects;
import java.util.Set;

public class Hook {
    private long id;
    private String partyId;
    private Set<WebhookAdditionalFilter> filters;
    private String url;
    private String pubKey;
    private String privKey;
    private boolean enabled;
    private RetryPolicyType retryPolicyType;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public Set<WebhookAdditionalFilter> getFilters() {
        return filters;
    }

    public void setFilters(Set<WebhookAdditionalFilter> filters) {
        this.filters = filters;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrivKey() {
        return privKey;
    }

    public void setPrivKey(String privKey) {
        this.privKey = privKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RetryPolicyType getRetryPolicyType() {
        return retryPolicyType;
    }

    public void setRetryPolicyType(RetryPolicyType retryPolicyType) {
        this.retryPolicyType = retryPolicyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hook)) return false;
        Hook hook = (Hook) o;
        return getId() == hook.getId() &&
                isEnabled() == hook.isEnabled() &&
                Objects.equals(getPartyId(), hook.getPartyId()) &&
                Objects.equals(getFilters(), hook.getFilters()) &&
                Objects.equals(getUrl(), hook.getUrl()) &&
                Objects.equals(getPubKey(), hook.getPubKey()) &&
                Objects.equals(getPrivKey(), hook.getPrivKey()) &&
                getRetryPolicyType() == hook.getRetryPolicyType();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getPartyId(), getFilters(), getUrl(), getPubKey(), getPrivKey(), isEnabled(), getRetryPolicyType());
    }

    @Override
    public String toString() {
        return "Hook{" +
                "id=" + id +
                ", partyId='" + partyId + '\'' +
                ", filters=" + filters +
                ", url='" + url + '\'' +
                ", pubKey='" + pubKey + '\'' +
                ", privKey='" + privKey + '\'' +
                ", enabled=" + enabled +
                ", retryPolicyType=" + retryPolicyType +
                '}';
    }

    public static class WebhookAdditionalFilter {
        private EventType eventType;
        private MessageType messageType;

        public WebhookAdditionalFilter() {
        }

        public WebhookAdditionalFilter(EventType eventType, MessageType messageType) {
            this.eventType = eventType;
            this.messageType = messageType;
        }

        public EventType getEventType() {
            return eventType;
        }

        public void setEventType(EventType eventType) {
            this.eventType = eventType;
        }

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WebhookAdditionalFilter)) return false;
            WebhookAdditionalFilter that = (WebhookAdditionalFilter) o;
            return getEventType() == that.getEventType() &&
                    getMessageType() == that.getMessageType();
        }

        @Override
        public int hashCode() {

            return Objects.hash(getEventType(), getMessageType());
        }

        @Override
        public String toString() {
            return "WebhookAdditionalFilter{" +
                    "eventType=" + eventType +
                    ", messageType=" + messageType +
                    '}';
        }
    }
}
