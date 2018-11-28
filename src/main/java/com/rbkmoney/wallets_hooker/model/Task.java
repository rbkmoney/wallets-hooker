package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

public class Task {

    private long messageId;
    private long queueId;

    public Task(long messageId, long queueId) {
        this.messageId = messageId;
        this.queueId = queueId;
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return getMessageId() == task.getMessageId() &&
                getQueueId() == task.getQueueId();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getMessageId(), getQueueId());
    }

    @Override
    public String toString() {
        return "Task{" +
                "messageId=" + messageId +
                ", queueId=" + queueId +
                '}';
    }

}
