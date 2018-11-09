package com.rbkmoney.wallets_hooker.dao;

public interface TaskDao {
    int createByMessageId(long messageId) throws DaoException;
    void remove(long queueId, long messageId);
    void removeAll(long queueId) throws DaoException;
}
