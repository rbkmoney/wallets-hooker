package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.QueueDao;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.model.WalletsQueue;
import com.rbkmoney.wallets_hooker.retry.RetryPoliciesService;
import com.rbkmoney.wallets_hooker.retry.RetryPolicy;
import com.rbkmoney.wallets_hooker.retry.RetryPolicyType;
import com.rbkmoney.swag_wallets_webhook_events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

/**
 * Created by inalarsanukaev on 14.11.17.
 */
public class WalletsQueueDao extends NamedParameterJdbcDaoSupport implements QueueDao<WalletsQueue> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RetryPoliciesService retryPoliciesService;

    public WalletsQueueDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public RowMapper<WalletsQueue> queueWithPolicyRowMapper = (rs, i) -> {
        WalletsQueue queue = new WalletsQueue();
        queue.setId(rs.getLong("id"));
        queue.setWalletId(rs.getString("wallet_id"));
        Hook hook = new Hook();
        hook.setId(rs.getLong("hook_id"));
        hook.setPartyId(rs.getString("party_id"));
        hook.setTopic(rs.getString("message_type"));
        hook.setUrl(rs.getString("url"));
        hook.setPubKey(rs.getString("pub_key"));
        hook.setPrivKey(rs.getString("priv_key"));
        hook.setEnabled(rs.getBoolean("enabled"));
        RetryPolicyType retryPolicyType = RetryPolicyType.valueOf(rs.getString("retry_policy"));
        RetryPolicy policy = retryPoliciesService.getRetryPolicyByType(retryPolicyType);
        hook.setRetryPolicyType(retryPolicyType);
        queue.setHook(hook);
        policy.fillQueue(queue, rs);
        return queue;
    };

    @Override
    public void createByMessageId(long messageId) throws DaoException {
        final String sql =
                " insert into whook.wallets_queue(hook_id, wallet_id)" +
                " select w.id , m.wallet_id" +
                " from whook.wallets_message m" +
                " join whook.webhook w on m.party_id = w.party_id and w.enabled and w.topic=CAST(:message_type as whook.message_topic)" +
                " where m.id = :id " +
                " on conflict(hook_id, wallet_id) do nothing";
        try {
            int count = getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", messageId)
                    .addValue("message_type", getMessagesTopic()));
            log.info("Created {} queues for messageId {}", count, messageId);
        } catch (NestedRuntimeException e) {
            log.error("Fail to createByMessageId queue {}", messageId, e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<WalletsQueue> getList(Collection<Long> ids) {
        final String sql =
                " select q.id, q.hook_id, q.wallet_id, q.fail_count, q.last_fail_time, q.next_time, wh.party_id, wh.url, wh.topic as message_type, k.pub_key, k.priv_key, wh.enabled, wh.retry_policy " +
                        " from whook.wallets_queue q " +
                        " join whook.webhook wh on wh.id = q.hook_id and wh.enabled and wh.topic=CAST(:message_type as whook.message_topic)" +
                        " join whook.party_key k on k.party_id = wh.party_id " +
                        " where q.id in (:ids) and q.enabled and :system_time >= coalesce(q.next_time, 0)";
        final MapSqlParameterSource params = new MapSqlParameterSource("ids", ids)
                .addValue("message_type", getMessagesTopic())
                .addValue("system_time", System.currentTimeMillis());
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, queueWithPolicyRowMapper);
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    public void updateRetries(WalletsQueue queue) {
        final String sql =
                " update whook.wallets_queue " +
                        " set last_fail_time = :last_fail_time, fail_count = :fail_count, next_time = :next_time" +
                        " where id = :id";
        try {
            RetryPolicyType retryPolicyType = queue.getHook().getRetryPolicyType();
            RetryPolicy policy = retryPoliciesService.getRetryPolicyByType(retryPolicyType);
            MapSqlParameterSource paramSource = policy.buildParamSource(queue);
            getNamedParameterJdbcTemplate().update(sql, paramSource);
            log.info("Record in table 'wallets_queue' with id {} updated.", queue.getId());
        } catch (NestedRuntimeException e) {
            log.warn("Fail to update wallets_queue with id {} ", queue.getId(), e);
            throw new DaoException(e);
        }
    }

    @Override
    public void disable(long id) throws DaoException {
        final String sql = " UPDATE whook.wallets_queue SET enabled = FALSE where id=:id;";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (NestedRuntimeException e) {
            log.error("Fail to disable queue: {}", id, e);
            throw new DaoException(e);
        }
    }

    public String getMessagesTopic() {
        return Event.TopicEnum.WALLETSTOPIC.getValue();
    }
}
