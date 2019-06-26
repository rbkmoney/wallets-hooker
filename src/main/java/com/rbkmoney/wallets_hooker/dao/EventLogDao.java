package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.constant.EventTopic;

public interface EventLogDao {

    Long getLastEventId(EventTopic eventTopic, Long defaultValue);

    void create(Long id, EventTopic eventType);

}
