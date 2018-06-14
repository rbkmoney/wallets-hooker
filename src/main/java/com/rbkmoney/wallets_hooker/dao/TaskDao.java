package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.Task;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TaskDao {
    void create(long messageId) throws DaoException;
    void remove(long queueId, long messageId);
    void removeAll(long queueId) throws DaoException;
    Map<Long, List<Task>> getScheduled(Collection<Long> excludeQueueIds) throws DaoException;
}
