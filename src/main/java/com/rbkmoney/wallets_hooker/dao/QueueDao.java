package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.Queue;

import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public interface QueueDao<Q extends Queue> {
    void createByMessageId(long messageId) throws DaoException;
    List<Q> getList(Collection<Long> ids);
    void updateRetries(Q queue);
    void disable(long id);
}
