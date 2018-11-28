package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

/**
 * Created by inalarsanukaev on 20.11.17.
 */
public abstract class Message {
    private Long id;
    private EventType eventType;
    private Long eventId;
    private String partyId;
    private String occuredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(String occuredAt) {
        this.occuredAt = occuredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message = (Message) o;
        return Objects.equals(getId(), message.getId()) &&
                getEventType() == message.getEventType() &&
                Objects.equals(getEventId(), message.getEventId()) &&
                Objects.equals(getPartyId(), message.getPartyId()) &&
                Objects.equals(getOccuredAt(), message.getOccuredAt());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getEventType(), getEventId(), getPartyId(), getOccuredAt());
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", eventType=" + eventType +
                ", eventId=" + eventId +
                ", partyId='" + partyId + '\'' +
                ", occuredAt='" + occuredAt + '\'' +
                '}';
    }
}
