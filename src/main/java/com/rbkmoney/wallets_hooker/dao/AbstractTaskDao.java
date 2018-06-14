package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractTaskDao extends NamedParameterJdbcDaoSupport implements TaskDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    public AbstractTaskDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public static RowMapper<Task> taskRowMapper = (rs, i) ->
            new Task(rs.getLong("message_id"), rs.getLong("queue_id"));

    protected abstract String getMessageTopic();

    @Override
    public void remove(long queueId, long messageId) throws DaoException {
        final String sql = "DELETE FROM whook.scheduled_task where queue_id=:queue_id and message_id=:message_id and message_type=CAST(:message_type as whook.message_topic)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("queue_id", queueId)
                    .addValue("message_id", messageId)
                    .addValue("message_type", getMessageTopic()));
            log.debug("Task with queueId {} messageId  {} removed from hook.scheduled_task", queueId, messageId);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to delete task by queue_id {} and message_id {}", queueId, messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public void removeAll(long queueId) throws DaoException {
        final String sql = "DELETE FROM whook.scheduled_task where queue_id=:queue_id and message_type=CAST(:message_type as whook.message_topic)";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("queue_id", queueId).addValue("message_type", getMessageTopic()));
        } catch (NestedRuntimeException e) {
            log.warn("Fail to delete tasks for hook:" + queueId, e);
            throw new DaoException(e);
        }
    }

    @Override
    // TODO think about limit
    public Map<Long, List<Task>> getScheduled(Collection<Long> excludeQueueIds) throws DaoException {
        final String sql =
                " SELECT st.message_id, st.queue_id FROM whook.scheduled_task st WHERE message_type=CAST(:message_type as whook.message_topic)" +
                        (excludeQueueIds.size() > 0 ? " AND st.queue_id not in (:queue_ids)" : "") +
                        " ORDER BY message_id ASC LIMIT 10000";
        try {
            List<Task> tasks = getNamedParameterJdbcTemplate().query(
                    sql, new MapSqlParameterSource()
                            .addValue("queue_ids", excludeQueueIds)
                            .addValue("message_type", getMessageTopic())
                    , taskRowMapper);
            return splitByQueue(tasks);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to get active tasks from scheduled_task", e);
            throw new DaoException(e);
        }
    }

    //should preserve order by message id
    private Map<Long, List<Task>> splitByQueue(List<Task> orderedByMessageIdTasks) {
        return orderedByMessageIdTasks.stream().collect(Collectors.groupingBy(Task::getQueueId));
    }
}
