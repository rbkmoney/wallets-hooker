package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.IdentityMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.IdentityMessage;
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
public class IdentityMessageDaoImpl extends NamedParameterJdbcDaoSupport implements IdentityMessageDao {

    public static final String ID = "id";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_ID = "event_id";
    public static final String PARTY_ID = "party_id";
    public static final String OCCURED_AT = "occured_at";
    public static final String IDENTITY_ID = "identity_id";


    private static RowMapper<IdentityMessage> messageRowMapper = (rs, i) -> {
        IdentityMessage message = new IdentityMessage();
        message.setId(rs.getLong(ID));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        message.setEventId(rs.getLong(EVENT_ID));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setOccuredAt(rs.getString(OCCURED_AT));
        message.setIdentityId(rs.getString(IDENTITY_ID));
        return message;
    };

    public IdentityMessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public IdentityMessage getAny(String identityId) throws DaoException {
        final String sql = "SELECT * FROM whook.identity_message WHERE identity_id =:identity_id ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(IDENTITY_ID, identityId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("IdentityMessage with identityId " + identityId + " not found!", e);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to get IdentityMessage with identityId " + identityId, e);
        }
    }

    @Override
    @Transactional
    public Long create(IdentityMessage message) throws DaoException {
        final String sql =
                "INSERT INTO whook.identity_message (event_type, event_id,  party_id, occured_at, identity_id) " +
                        "VALUES (CAST(:event_type as whook.event_type), :event_id,  :party_id, :occured_at, :identity_id) " +
                        "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_TYPE, message.getEventType().name())
                .addValue(EVENT_ID, message.getEventId())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(OCCURED_AT, message.getOccuredAt())
                .addValue(IDENTITY_ID, message.getIdentityId())
                ;
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            return keyHolder.getKey().longValue();
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to create IdentityMessage with identityId "+ message.getIdentityId(), e);
        }
    }

    @Override
    public Long getLastEventId() {
        final String sql = "SELECT max(event_id) FROM whook.identity_message";
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<>(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<IdentityMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM whook.identity_message WHERE id in (:ids)";
        try {
            List<IdentityMessage> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            return messagesFromDb;
        }  catch (NestedRuntimeException e) {
            throw new DaoException("Error to get list of IdentityMessage with ids " + messageIds, e);
        }
    }
}
