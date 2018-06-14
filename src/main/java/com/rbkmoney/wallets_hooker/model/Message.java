package com.rbkmoney.wallets_hooker.model;

import com.rbkmoney.swag_wallets_webhook_events.Event;

import java.util.Objects;

/**
 * Created by inalarsanukaev on 20.11.17.
 */
public class Message {
    private Long id;
    private Event event;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return Objects.equals(getId(), message.getId()) &&
                Objects.equals(getEvent(), message.getEvent());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getEvent());
    }
}
