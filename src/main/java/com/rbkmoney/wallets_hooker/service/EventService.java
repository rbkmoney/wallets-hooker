package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    WalletsMessageDao messageDao;

    public Long getLastEventId() {
        Long maxId = messageDao.getMaxEventId();
        log.info("Get last event id = {}", maxId);
        return maxId;
    }
}
