package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.domain.enums.EventTopic;

public interface EventLogDao {

    void create(String sourceId, Long eventId, EventTopic eventTopic);

}
