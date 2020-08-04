package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.domain.enums.EventTopic;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.EventLog;
import com.rbkmoney.wallets_hooker.domain.tables.records.EventLogRecord;
import org.jooq.InsertReturningStep;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.wallets_hooker.domain.Tables.EVENT_LOG;

@Component
public class EventLogDaoImpl extends AbstractDao implements EventLogDao {

    public EventLogDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void create(String sourceId, Long eventId, EventTopic eventTopic) {
        EventLog eventLog = new EventLog();
        eventLog.setSourceId(sourceId);
        eventLog.setEventId(eventId);
        eventLog.setEventTopic(eventTopic);
        InsertReturningStep<EventLogRecord> insertReturningStep = getDslContext()
                .insertInto(EVENT_LOG)
                .set(getDslContext()
                        .newRecord(EVENT_LOG, eventLog))
                .onConflict(EVENT_LOG.SOURCE_ID, EVENT_LOG.EVENT_ID, EVENT_LOG.EVENT_TOPIC)
                .doNothing();
        execute(insertReturningStep);
    }
}
