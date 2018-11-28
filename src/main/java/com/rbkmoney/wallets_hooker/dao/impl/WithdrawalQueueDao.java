package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.QueueDao;
import com.rbkmoney.wallets_hooker.model.*;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@DependsOn("dbInitializer")
public class WithdrawalQueueDao extends NamedParameterJdbcDaoSupport implements QueueDao<WithdrawalQueue> {

    private long[] delays = {30, 300, 900, 3600,
            3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600,
            3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600,
            3600, 3600, 3600, 3600
    }; //in seconds

    public WithdrawalQueueDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public RowMapper<TaskQueuePair<WithdrawalQueue>> queueWithPolicyRowMapper = (rs, i) -> {
        Task task = new Task(rs.getLong("message_id"), rs.getLong("id"));
        WithdrawalQueue queue = new WithdrawalQueue();
        queue.setId(rs.getLong("id"));
        queue.setWithdrawalId(rs.getString("withdrawal_id"));
        Hook hook = new Hook();
        hook.setId(rs.getLong("hook_id"));
        hook.setPartyId(rs.getString("party_id"));
        hook.setUrl(rs.getString("url"));
        hook.setPubKey(rs.getString("pub_key"));
        hook.setPrivKey(rs.getString("priv_key"));
        hook.setEnabled(rs.getBoolean("enabled"));
        queue.setHook(hook);
        queue.setFailCount(rs.getInt("fail_count"));
        queue.setLastFailTime(rs.getLong("last_fail_time"));
        queue.setNextTime(rs.getLong("next_time"));
        return new TaskQueuePair<>(task, queue);
    };

    @Override
    public int createByMessageId(long messageId) throws DaoException {
        final String sql =
                " insert into whook.withdrawal_queue(hook_id, withdrawal_id)" +
                " select w.id , m.withdrawal_id" +
                " from whook.withdrawal_message m" +
                " join whook.webhook w on m.party_id = w.party_id and w.enabled and exists " +
                        "(select * from whook.webhook_to_events wte " +
                        "where wte.hook_id = w.id AND wte.message_type=CAST(:message_type as whook.message_type))" +
                " where m.id = :id " +
                " on conflict(hook_id, withdrawal_id) do nothing";
        try {
            int count = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", messageId)
                    .addValue("message_type", getMessageType()));
            return count;
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to create queue by messageId " + messageId, e);
        }
    }

    @Override
    public Map<Long, List<TaskQueuePair<WithdrawalQueue>>> getTaskQueuePairsMap(Collection<Long> excludeQueueIds) {
        final String sql =
                " select st.message_id, q.id, q.hook_id, q.withdrawal_id, q.fail_count, q.last_fail_time, q.next_time, wh.party_id, wh.url, k.pub_key, k.priv_key, wh.enabled " +
                        " from whook.scheduled_task st "+
                        " join whook.withdrawal_queue q on st.queue_id = q.id AND st.message_type=CAST(:message_type as whook.message_type)" +
                        " join whook.webhook wh on wh.id = q.hook_id and wh.enabled " +
                        " join whook.party_key k on k.party_id = wh.party_id " +
                        " where st.message_type=CAST(:message_type as whook.message_type) "+
                        (excludeQueueIds.size() > 0 ? " AND q.id not in (:queue_ids)" : "") +
                        " and q.enabled and :system_time >= coalesce(q.next_time, 0) " +
                        " ORDER BY message_id ASC limit 1000";
        final MapSqlParameterSource params = new MapSqlParameterSource("queue_ids", excludeQueueIds)
                .addValue("message_type", getMessageType())
                .addValue("system_time", System.currentTimeMillis());
        try {
            return splitByQueue(getNamedParameterJdbcTemplate().query(sql, params, queueWithPolicyRowMapper));
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to get task queue pairs", e);
        }
    }

    //should preserve order by message id
    private Map<Long, List<TaskQueuePair<WithdrawalQueue>>> splitByQueue(List<TaskQueuePair<WithdrawalQueue>> orderedByMessageIdTasks) {
        return orderedByMessageIdTasks.stream().collect(Collectors.groupingBy(x -> x.getQueue().getId()));
    }

    @Override
    public void updateRetries(WithdrawalQueue queue) {
        final String sql =
                " update whook.withdrawal_queue " +
                        " set last_fail_time = :last_fail_time, fail_count = :fail_count, next_time = :next_time" +
                        " where id = :id";
        try {
            MapSqlParameterSource paramSource = new MapSqlParameterSource("id", queue.getId())
                    .addValue("fail_count", queue.getFailCount())
                    .addValue("last_fail_time", queue.getLastFailTime())
                    .addValue("next_time", queue.getNextTime());
            getNamedParameterJdbcTemplate().update(sql, paramSource);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to update withdrawal_queue with id " + queue.getId(), e);
        }
    }

    @Override
    public void disable(long id) throws DaoException {
        final String sql = " UPDATE whook.withdrawal_queue SET enabled = FALSE where id=:id;";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to disable queue with id " + id, e);
        }
    }

    @Override
    public boolean shouldDisable(WithdrawalQueue queue) {
        queue.setFailCount(queue.getFailCount() + 1);
        queue.setLastFailTime(System.currentTimeMillis());
        boolean isFail = queue.getFailCount() > delays.length;
        if (!isFail) {
            queue.setNextTime(queue.getLastFailTime() + (delays[queue.getFailCount() - 1] * 1000));
        }
        return isFail;
    }

    public String getMessageType() {
        return MessageType.WITHDRAWAL.name();
    }
}
