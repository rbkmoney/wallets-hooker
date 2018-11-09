package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.AbstractTaskDao;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.model.MessageType;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@DependsOn("dbInitializer")
public class WithdrawalTaskDao extends AbstractTaskDao {

    public WithdrawalTaskDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getMessageType() {
        return MessageType.WITHDRAWAL.name();
    }

    @Override
    public int createByMessageId(long messageId) throws DaoException {
        final String sql =
                " insert into whook.scheduled_task(message_id, queue_id, message_type)" +
                        " select m.id, q.id, CAST(:message_type as whook.message_type)" +
                        " from whook.withdrawal_message m" +
                        " join whook.webhook w on m.party_id = w.party_id and w.enabled " +
                        " join whook.webhook_to_events wte on wte.hook_id = w.id and wte.message_type=CAST(:message_type as whook.message_type)" +
                        " join whook.withdrawal_queue q on q.hook_id=w.id and q.enabled and q.withdrawal_id=m.withdrawal_id" +
                        " where m.id = :message_id " +
                        " and m.event_type = wte.event_type " +
                        " ON CONFLICT (message_id, queue_id, message_type) DO NOTHING";
        try {
            return getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("message_id", messageId)
                    .addValue("message_type", getMessageType()));
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to create task by messageId " + messageId, e);
        }
    }
}
