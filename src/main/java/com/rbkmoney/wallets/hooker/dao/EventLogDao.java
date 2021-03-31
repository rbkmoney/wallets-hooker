package com.rbkmoney.wallets.hooker.dao;

import com.rbkmoney.wallets.hooker.domain.enums.EventTopic;
import com.rbkmoney.wallets.hooker.domain.tables.pojos.EventLog;

import java.util.Optional;

public interface EventLogDao {

    Optional<EventLog> get(String sourceId, Long eventId, EventTopic eventTopic);

    void create(String sourceId, Long eventId, EventTopic eventTopic);

}
