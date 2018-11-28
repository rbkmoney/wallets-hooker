package com.rbkmoney.wallets_hooker.model;

public class TaskQueuePair<Q extends Queue> {
    private Task task;
    private Q queue;

    public TaskQueuePair(Task task, Q queue) {
        this.task = task;
        this.queue = queue;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Q getQueue() {
        return queue;
    }

    public void setQueue(Q queue) {
        this.queue = queue;
    }
}
