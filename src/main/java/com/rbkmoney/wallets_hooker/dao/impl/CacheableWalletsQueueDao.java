package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.CacheMng;
import com.rbkmoney.wallets_hooker.model.WalletsQueue;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CacheableWalletsQueueDao extends WalletsQueueDao {
    @Autowired
    CacheMng cacheMng;

    public CacheableWalletsQueueDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<WalletsQueue> getList(Collection<Long> ids) {
        List<WalletsQueue> queues = cacheMng.getQueues(ids, WalletsQueue.class);
        if (queues.size() == ids.size()) {
            return queues;
        }
        Set<Long> cacheIds = new HashSet<>(ids);
        queues.forEach(h -> cacheIds.remove(h.getId()));
        List<WalletsQueue> queuesFromDb = super.getList(cacheIds);
        cacheMng.putQueues(queuesFromDb);
        queues.addAll(queuesFromDb);
        return queues;
    }
}
