package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.domain.enums.EventTopic;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.EventLog;

import java.util.Optional;

public interface EventLogDao {

    Optional<EventLog> get(String sourceId, Long eventId, EventTopic eventTopic);

    void create(String sourceId, Long eventId, EventTopic eventTopic);

}
