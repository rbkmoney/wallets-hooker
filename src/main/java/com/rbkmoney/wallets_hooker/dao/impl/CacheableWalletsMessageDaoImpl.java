package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.CacheMng;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

 public class CacheableWalletsMessageDaoImpl extends WalletsMessageDaoImpl {

    @Autowired
    private CacheMng cacheMng;

    public CacheableWalletsMessageDaoImpl(DataSource dataSource) {
        super(dataSource);
    }
    @Override
    public void create(WalletsMessage message) throws DaoException {
        super.create(message);
        putToCache(message);
    }

    @Override
    public List<WalletsMessage> getBy(Collection<Long> ids) throws DaoException {
        List<WalletsMessage> messages = cacheMng.getMessages(ids, WalletsMessage.class);
        if (messages.size() == ids.size()) {
            return messages;
        }
        Set<Long> cacheIds = new HashSet<>(ids);
        messages.forEach(m -> cacheIds.remove(m.getId()));
        List<WalletsMessage> messagesFromDb = super.getBy(cacheIds);
        messagesFromDb.forEach(this::putToCache);
        messages.addAll(messagesFromDb);
        return messages;
    }

    private void putToCache(WalletsMessage message){
        if (message != null) {
            cacheMng.putMessage(message);
            cacheMng.putMessage(message.getWalletId(), message);
        }
    }
}
