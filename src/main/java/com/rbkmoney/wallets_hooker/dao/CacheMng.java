package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.Message;
import com.rbkmoney.wallets_hooker.model.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by inalarsanukaev on 23.11.17.
 */
@Component
public class CacheMng {
    private static final String MESSAGES_BY_QUEUE_QUALIFIER = "messagesByQueueQualifier";
    private static final String MESSAGES_BY_IDS = "messagesById";
    private static final String QUEUES = "queues";

    @Autowired
    private CacheManager cacheMng;

    public void putMessage(Message message){
        cacheMng.getCache(MESSAGES_BY_IDS).put(message.getId(), message);
    }

    public void putMessage(String id, Message message){
        cacheMng.getCache(MESSAGES_BY_QUEUE_QUALIFIER).put(id, message);
    }

    public <T extends Message> List<T> getMessages(Collection<Long> ids, Class<T> type) {
        Cache cache = cacheMng.getCache(MESSAGES_BY_IDS);
        return ids.stream().map(id -> cache.get(id, type)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public  <T extends Queue> List<T> getQueues(Collection<Long> ids, Class<T> type){
        Cache cache = cacheMng.getCache(QUEUES);
        return ids.stream().map(id -> cache.get(id, type)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void putQueues(Collection<? extends Queue> queues){
        Cache cache = cacheMng.getCache(QUEUES);
        queues.forEach(q -> cache.put(q.getId(), q));
    }
}
