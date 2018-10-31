package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.Queue;
import com.rbkmoney.wallets_hooker.model.TaskQueuePair;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public interface QueueDao<Q extends Queue> {
    void createByMessageId(long messageId) throws DaoException;
    Map<Long, List<TaskQueuePair<Q>>> getTaskQueuePairsMap(Collection<Long> ids);
    void updateRetries(Q queue);
    void disable(long id);
}
