package com.rbkmoney.wallets_hooker.dao;

public interface TaskDao {
    void create(long messageId) throws DaoException;
    void remove(long queueId, long messageId);
    void removeAll(long queueId) throws DaoException;
}
