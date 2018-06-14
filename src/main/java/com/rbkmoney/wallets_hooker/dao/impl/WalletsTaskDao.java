package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.AbstractTaskDao;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.swag_wallets_webhook_events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.sql.DataSource;

public class WalletsTaskDao extends AbstractTaskDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public WalletsTaskDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getMessageTopic() {
        return Event.TopicEnum.WALLETSTOPIC.getValue();
    }

    //TODO limit from hook
    @Override
    public void create(long messageId) throws DaoException {
        final String sql =
                " insert into whook.scheduled_task(message_id, queue_id, message_type)" +
                        " select m.id, q.id, w.topic" +
                        " from whook.wallets_message m" +
                        " join whook.webhook w on m.party_id = w.party_id and w.enabled and w.topic=CAST(:message_type as whook.message_topic)" +
                        " join whook.webhook_to_events wte on wte.hook_id = w.id" +
                        " join whook.wallets_queue q on q.hook_id=w.id and q.enabled and q.wallet_id=m.wallet_id" +
                        " where m.id = :message_id " +
                        " and m.event_type = wte.event_type " +
                        " ON CONFLICT (message_id, queue_id, message_type) DO NOTHING";
        try {
            int updateCount = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
            log.info("Created tasks count={} for messageId={}", updateCount, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to createByMessageId tasks for messages.", e);
            throw new DaoException(e);
        }
    }
}
