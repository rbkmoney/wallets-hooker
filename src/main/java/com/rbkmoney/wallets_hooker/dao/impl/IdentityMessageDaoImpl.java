package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.IdentityMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.IdentityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        IdentityMessage result = null;
        final String sql = "SELECT * FROM whook.identity_message WHERE identity_id =:identity_id ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(IDENTITY_ID, identityId);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.warn("IdentityMessage with id {} not exist!", identityId);
        } catch (NestedRuntimeException e) {
            throw new DaoException("IdentityMessageDaoImpl.getAny error with identityId " + identityId, e);
        }
        return result;
    }

    @Override
    @Transactional
    public void create(IdentityMessage message) throws DaoException {
        final String sql =
                "INSERT INTO whook.identity_message (event_type, event_id,  party_id, occured_at, identity_id) " +
                        "VALUES (CAST(:event_type as whook.EventType), :event_id,  :party_id, :occured_at, :identity_id) " +
                        "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_TYPE, message.getEventType().name())
                .addValue(EVENT_ID, message.getEventId())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(OCCURED_AT, message.getOccuredAt())
                .addValue(IDENTITY_ID, message.getId())
                ;
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            message.setId(keyHolder.getKey().longValue());
            log.info("IdentityMessage {} saved to db.", message);
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't createByMessageId message with identityId "+ message.getIdentityId(), e);
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
            log.debug("messagesFromDb {}", messagesFromDb);
            return messagesFromDb;
        }  catch (NestedRuntimeException e) {
            throw new DaoException("IdentityMessageDaoImpl.getByIds error", e);
        }
    }
}
