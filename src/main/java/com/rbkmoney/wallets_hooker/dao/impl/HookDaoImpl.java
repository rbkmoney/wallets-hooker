package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.HookDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.model.MessageType;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@DependsOn("dbInitializer")
public class HookDaoImpl extends NamedParameterJdbcDaoSupport implements HookDao {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final Signer signer;

    public HookDaoImpl(DataSource dataSource, Signer signer) {
        setDataSource(dataSource);
        this.signer = signer;
    }

    @Override
    public List<Hook> getPartyHooks(String partyId) throws DaoException {
        log.debug("getPartyHooks request. PartyId = {}", partyId);
        final String sql =
                " select w.*, k.pub_key, wte.* " +
                        " from whook.webhook w " +
                        " join whook.party_key k " +
                        " on w.party_id = k.party_id " +
                        " join whook.webhook_to_events wte " +
                        " on wte.hook_id = w.id " +
                        " where w.party_id =:party_id " +
                        " order by id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("party_id", partyId);

        try {
            List<AllHookTablesRow> allHookTablesRows = getNamedParameterJdbcTemplate().query(sql, params, allHookTablesRowRowMapper);
            List<Hook> result = squashToWebhooks(allHookTablesRows);
            log.debug("getPartyHooks response. Hooks: " + result);
            return result;
        } catch (NestedRuntimeException e) {
            String message = "Couldn't getPartyHooks for partyId " + partyId;
            log.warn(message, e);
            throw new DaoException(message);
        }
    }

    private List<Hook> squashToWebhooks(List<AllHookTablesRow> allHookTablesRows) {
        Map<Long, List<AllHookTablesRow>> map = allHookTablesRows.stream().collect(Collectors.groupingBy(AllHookTablesRow::getId));
        return map.entrySet().stream().map(Map.Entry::getValue).map(rows -> {
            Hook hook = new Hook();
            AllHookTablesRow row = rows.get(0);
            hook.setId(row.getId());
            hook.setPartyId(row.getPartyId());
            hook.setUrl(row.getUrl());
            hook.setPubKey(row.getPubKey());
            hook.setEnabled(row.isEnabled());
            hook.setFilters(rows.stream().map(AllHookTablesRow::getWebhookAdditionalFilter).collect(Collectors.toSet()));
            return hook;
        }).collect(Collectors.toList());
    }

    @Override
    public Hook getHookById(long id) throws DaoException {
        final String sql = "select w.*, k.pub_key, wte.* " +
                "from whook.webhook w " +
                "join whook.party_key k " +
                "on w.party_id = k.party_id " +
                "join whook.webhook_to_events wte " +
                "on wte.hook_id = w.id " +
                "where w.id =:id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        try {
            List<AllHookTablesRow> allHookTablesRows = getNamedParameterJdbcTemplate().query(sql, params, allHookTablesRowRowMapper);
            List<Hook> result = squashToWebhooks(allHookTablesRows);
            if (result == null || result.isEmpty()) {
                return null;
            }
            if (result.size() > 1) {
                throw new DaoException("Unexpected size(" + result.size() + ") of query for getHookById(" + id + "), it must be 1.");
            }
            return result.get(0);
        } catch (NestedRuntimeException e) {
            log.warn("Fail to get hook {}", id, e);
            throw new DaoException(e);
        }
    }

    @Override
    @Transactional
    public Hook create(Hook hook) throws DaoException {
        String pubKey = createOrGetPubKey(hook.getPartyId());
        hook.setPubKey(pubKey);
        hook.setEnabled(true);

        final String sql = "INSERT INTO whook.webhook(party_id, url) " +
                "VALUES (:party_id, :url) RETURNING ID";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("party_id", hook.getPartyId())
                .addValue("url", hook.getUrl());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            int updateCount = getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            if (updateCount != 1) {
                throw new DaoException("Couldn't insert webhook " + hook.getId() + " into table");
            }
            hook.setId(keyHolder.getKey().longValue());
            saveHookFilters(hook.getId(), hook.getFilters());
        } catch (NestedRuntimeException e) {
            log.warn("Fail to createByMessageId hook {}", hook, e);
            throw new DaoException(e);
        }
        log.info("Webhook with id = {} created.", hook.getId());
        return hook;
    }

    private void saveHookFilters(long hookId, Collection<Hook.WebhookAdditionalFilter> webhookAdditionalFilters) {
        int size = webhookAdditionalFilters.size();
        List<Map<String, Object>> batchValues = new ArrayList<>(size);
        for (Hook.WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("hook_id", hookId)
                    .addValue("message_type", webhookAdditionalFilter.getMessageType().name())
                    .addValue("event_type", webhookAdditionalFilter.getEventType().name());
            batchValues.add(mapSqlParameterSource.getValues());
        }

        final String sql = "INSERT INTO whook.webhook_to_events(hook_id, message_type, event_type) " +
                "VALUES (:hook_id, CAST(:message_type AS whook.message_type), CAST(:event_type AS whook.event_type))";
        try {
            int updateCount[] = getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[size]));
            if (updateCount.length != size) {
                throw new DaoException("Couldn't insert relation between hook and events.");
            }
        } catch (NestedRuntimeException e) {
            log.error("Fail to save hook filters.", e);
            throw new DaoException(e);
        }
    }

    @Override
    @Transactional
    public void delete(long id) throws DaoException {
        final String sql =
                " DELETE FROM whook.scheduled_task st USING whook.withdrawal_queue q WHERE st.queue_id = q.id AND q.hook_id=:id;" +
                " DELETE FROM whook.withdrawal_queue where hook_id=:id;" +
                " DELETE FROM whook.webhook_to_events where hook_id=:id;" +
                " DELETE FROM whook.webhook where id=:id; ";
        try {
            getNamedParameterJdbcTemplate().update(sql, new MapSqlParameterSource("id", id));
        } catch (NestedRuntimeException e) {
            throw new DaoException(e);
        }
    }

    private String createOrGetPubKey(String partyId) throws DaoException {
        final String sql = "INSERT INTO whook.party_key(party_id, priv_key, pub_key) " +
                "VALUES (:party_id, :priv_key, :pub_key) " +
                "ON CONFLICT(party_id) DO UPDATE SET party_id=:party_id RETURNING pub_key";

        KeyPair keyPair = signer.generateKeys();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("party_id", partyId)
                .addValue("priv_key", keyPair.getPrivKey())
                .addValue("pub_key", keyPair.getPublKey());
        String pubKey = null;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            pubKey = (String) keyHolder.getKeys().get("pub_key");
        } catch (NestedRuntimeException | NullPointerException | ClassCastException e) {
            log.warn("Fail to createOrGetPubKey security keys for party {} ", partyId,  e);
            throw new DaoException(e);
        }
        return pubKey;
    }


    private static RowMapper<AllHookTablesRow> allHookTablesRowRowMapper =
            (rs, i) -> new AllHookTablesRow(rs.getLong("id"),
                    rs.getString("party_id"),
                    rs.getString("url"),
                    rs.getString("pub_key"),
                    rs.getBoolean("enabled"),
                    new Hook.WebhookAdditionalFilter(EventType.valueOf(rs.getString("event_type")),
                            MessageType.valueOf(rs.getString("message_type"))));
}