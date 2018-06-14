package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.HookDao;
import com.rbkmoney.wallets_hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

public class HookDaoImpl implements HookDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Signer signer;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public HookDaoImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
            List<AllHookTablesRow> allHookTablesRows = jdbcTemplate.query(sql, params, allHookTablesRowRowMapper);
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
        List<Hook> result = new ArrayList<>();
        if (allHookTablesRows == null || allHookTablesRows.isEmpty()) {
            return result;
        }
        final Map<Long, List<AllHookTablesRow>> hookIdToRows = new HashMap<>();

        //grouping by hookId
        for (AllHookTablesRow row : allHookTablesRows) {
            final long hookId = row.getId();
            List<AllHookTablesRow> list = hookIdToRows.computeIfAbsent(hookId, k -> new ArrayList<>());
            list.add(row);
        }

        for (long hookId : hookIdToRows.keySet()) {
            List<AllHookTablesRow> rows = hookIdToRows.get(hookId);
            Hook hook = new Hook();
            hook.setId(hookId);
            hook.setPartyId(rows.get(0).getPartyId());
            hook.setTopic(rows.get(0).getTopic());
            hook.setUrl(rows.get(0).getUrl());
            hook.setPubKey(rows.get(0).getPubKey());
            hook.setEnabled(rows.get(0).isEnabled());
            hook.setFilters(rows.stream().map(r -> r.getWebhookAdditionalFilter()).collect(Collectors.toSet()));
            result.add(hook);
        }

        return result;
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
            List<AllHookTablesRow> allHookTablesRows = jdbcTemplate.query(sql, params, allHookTablesRowRowMapper);
            List<Hook> result = squashToWebhooks(allHookTablesRows);
            if (result == null || result.isEmpty()) {
                return null;
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

        final String sql = "INSERT INTO whook.webhook(party_id, url, topic) " +
                "VALUES (:party_id, :url, CAST(:topic as whook.message_topic)) RETURNING ID";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("party_id", hook.getPartyId())
                .addValue("url", hook.getUrl())
                .addValue("topic", hook.getTopic());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            int updateCount = jdbcTemplate.update(sql, params, keyHolder);
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

    private void saveHookFilters(long hookId, Collection<WebhookAdditionalFilter> webhookAdditionalFilters) {
        int size = webhookAdditionalFilters.size();
        List<Map<String, Object>> batchValues = new ArrayList<>(size);
        for (WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
            MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("hook_id", hookId)
                    .addValue("event_type", webhookAdditionalFilter.getEventType().toString());
            batchValues.add(mapSqlParameterSource.getValues());
        }

        final String sql = "INSERT INTO whook.webhook_to_events(hook_id, event_type) VALUES (:hook_id, CAST(:event_type AS whook.eventtype))";
        try {
            int updateCount[] = jdbcTemplate.batchUpdate(sql, batchValues.toArray(new Map[size]));
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
                " DELETE FROM whook.scheduled_task st USING whook.wallets_queue q WHERE st.queue_id = q.id AND q.hook_id=:id;" +
                " DELETE FROM whook.wallets_queue where hook_id=:id;" +
                " DELETE FROM whook.webhook_to_events where hook_id=:id;" +
                " DELETE FROM whook.webhook where id=:id; ";
        try {
            jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
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
            jdbcTemplate.update(sql, params, keyHolder);
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
                    rs.getString("topic"),
                    rs.getString("url"),
                    rs.getString("pub_key"),
                    rs.getBoolean("enabled"),
                    new WebhookAdditionalFilter(EventType.valueOf(rs.getString("event_type"))));
}