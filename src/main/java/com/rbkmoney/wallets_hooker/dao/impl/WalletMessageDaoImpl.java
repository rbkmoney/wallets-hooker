package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
@DependsOn("dbInitializer")
public class WalletMessageDaoImpl extends NamedParameterJdbcDaoSupport implements WalletMessageDao {

    public static final String ID = "id";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_ID = "event_id";
    public static final String PARTY_ID = "party_id";
    public static final String OCCURED_AT = "occured_at";
    public static final String WALLET_ID = "wallet_id";
    public static final String IDENTITY_ID = "identity_id";

    private static RowMapper<WalletMessage> messageRowMapper = (rs, i) -> {
        WalletMessage message = new WalletMessage();
        message.setId(rs.getLong(ID));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        message.setEventId(rs.getLong(EVENT_ID));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setOccuredAt(rs.getString(OCCURED_AT));
        message.setWalletId(rs.getString(WALLET_ID));
        message.setIdentityId(rs.getString(IDENTITY_ID));
        return message;
    };

    public WalletMessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public WalletMessage getAny(String walletId) throws DaoException {
        final String sql = "SELECT * FROM whook.wallet_message WHERE wallet_id =:wallet_id ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(WALLET_ID, walletId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("WalletMessage with walletId " + walletId + " not found.");
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to get WalletMessage with walletId " + walletId, e);
        }
    }

    @Override
    @Transactional
    public Long create(WalletMessage message) throws DaoException {
        final String sql =
                "INSERT INTO whook.wallet_message (event_type, event_id,  party_id, occured_at, wallet_id, identity_id) " +
                        "VALUES (CAST(:event_type as whook.event_type), :event_id,  :party_id, :occured_at, :wallet_id, :identity_id) " +
                        "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_TYPE, message.getEventType().name())
                .addValue(EVENT_ID, message.getEventId())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(OCCURED_AT, message.getOccuredAt())
                .addValue(WALLET_ID, message.getWalletId())
                .addValue(IDENTITY_ID, message.getIdentityId())
                ;
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            return keyHolder.getKey().longValue();
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to create WalletMessage with walletId " + message.getWalletId(), e);
        }
    }

    @Override
    public Long getLastEventId() {
        final String sql = "SELECT max(event_id) FROM whook.wallet_message";
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<>(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<WalletMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM whook.wallet_message WHERE id in (:ids)";
        try {
            List<WalletMessage> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            return messagesFromDb;
        }  catch (NestedRuntimeException e) {
            throw new DaoException("Error to get list of WalletMessage with ids " + messageIds, e);
        }
    }
}
