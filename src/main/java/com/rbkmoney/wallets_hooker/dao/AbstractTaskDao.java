package com.rbkmoney.wallets_hooker.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;


public abstract class AbstractTaskDao extends NamedParameterJdbcDaoSupport implements TaskDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractTaskDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    protected abstract String getMessageType();

    @Override
    public void remove(long queueId, long messageId) throws DaoException {
        final String sql = "DELETE FROM whook.scheduled_task where queue_id=:queue_id and message_id=:message_id and message_type=CAST(:message_type as whook.message_type)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("queue_id", queueId)
                    .addValue("message_id", messageId)
                    .addValue("message_type", getMessageType()));
            log.debug("Task with queueId {} messageId  {} removed from hook.scheduled_task", queueId, messageId);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to delete task by queue_id {} and message_id {}", queueId, messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public void removeAll(long queueId) throws DaoException {
        final String sql = "DELETE FROM whook.scheduled_task where queue_id=:queue_id and message_type=CAST(:message_type as whook.message_type)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("queue_id", queueId).addValue("message_type", getMessageType()));
        } catch (NestedRuntimeException e) {
            log.warn("Fail to delete tasks for hook:" + queueId, e);
            throw new DaoException(e);
        }
    }
}
