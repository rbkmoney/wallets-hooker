package com.rbkmoney.wallets_hooker.dao;

import java.util.Collection;
import java.util.List;

public interface MessageDao<M> {
    void create(M message) throws DaoException;
    Long getMaxEventId();
    List<M> getBy(Collection<Long> messageIds) throws DaoException;
}
