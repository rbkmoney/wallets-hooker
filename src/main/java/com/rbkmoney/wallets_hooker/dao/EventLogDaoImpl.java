package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets_hooker.constant.EventTopic;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.EventLog;
import com.rbkmoney.wallets_hooker.domain.tables.records.EventLogRecord;
import org.jooq.InsertReturningStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.wallets_hooker.domain.Tables.EVENT_LOG;
import static org.jooq.impl.DSL.max;

@Component
public class EventLogDaoImpl extends AbstractDao implements EventLogDao {

    private final RowMapper<EventLog> listRecordRowMapper;

    public EventLogDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.listRecordRowMapper = new RecordRowMapper<>(EVENT_LOG, EventLog.class);
    }

    @Override
    public Long getLastEventId(EventTopic eventTopic, Long defaultValue) {
        EventLog eventLog = fetchOne(getDslContext()
                        .select(max(EVENT_LOG.EVENT_ID).as(EVENT_LOG.EVENT_ID.getName()))
                        .from(EVENT_LOG)
                        .where(EVENT_LOG.EVENT_SINK.eq(eventTopic.name())),
                listRecordRowMapper);
        return eventLog != null ? eventLog.getEventId() : defaultValue;
    }

    @Override
    public void create(Long id, EventTopic eventType) {
        EventLog eventLog = new EventLog();
        eventLog.setEventId(id);
        eventLog.setEventSink(eventType.name());
        InsertReturningStep<EventLogRecord> insertReturningStep = getDslContext()
                .insertInto(EVENT_LOG)
                .set(getDslContext()
                        .newRecord(EVENT_LOG, eventLog))
                .onConflict(EVENT_LOG.EVENT_ID)
                .doNothing();
        execute(insertReturningStep);
    }
}
