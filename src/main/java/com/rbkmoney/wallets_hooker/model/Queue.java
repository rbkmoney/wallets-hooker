package com.rbkmoney.wallets_hooker.model;

import java.util.Objects;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class Queue {
    private long id;
    private Hook hook;
    private int failCount;
    private long lastFailTime;
    private long nextTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Hook getHook() {
        return hook;
    }

    public void setHook(Hook hook) {
        this.hook = hook;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public long getLastFailTime() {
        return lastFailTime;
    }

    public void setLastFailTime(long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }

    public long getNextTime() {
        return nextTime;
    }

    public void setNextTime(long nextTime) {
        this.nextTime = nextTime;
    }

    public boolean isFailed() {
        return failCount > 0;
    }

    public void resetRetries() {
        failCount = 0;
        lastFailTime = 0;
        nextTime = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Queue)) return false;
        Queue queue = (Queue) o;
        return getId() == queue.getId() &&
                getFailCount() == queue.getFailCount() &&
                getLastFailTime() == queue.getLastFailTime() &&
                getNextTime() == queue.getNextTime() &&
                Objects.equals(getHook(), queue.getHook());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getHook(), getFailCount(), getLastFailTime(), getNextTime());
    }

    @Override
    public String toString() {
        return "Queue{" +
                "id=" + id +
                ", hook=" + hook +
                ", failCount=" + failCount +
                ", lastFailTime=" + lastFailTime +
                ", nextTime=" + nextTime +
                '}';
    }
}
