package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.EventType;

/**
 * Created by inalarsanukaev on 18.04.17.
 */
public class WebhookAdditionalFilter {
    private EventType eventType;

    public WebhookAdditionalFilter(EventType eventType) {
        this.eventType = eventType;
    }

    public WebhookAdditionalFilter() {

    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
