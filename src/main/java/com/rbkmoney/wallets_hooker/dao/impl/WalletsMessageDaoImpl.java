package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.wallets_hooker.dao.CacheMng;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.rbkmoney.wallets_hooker.utils.ConverterUtils.buildWalletsMessage;

public class WalletsMessageDaoImpl extends NamedParameterJdbcDaoSupport implements WalletsMessageDao {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CacheMng cacheMng;

    @Autowired
    private WalletsQueueDao queueDao;

    @Autowired
    private WalletsTaskDao taskDao;

    public static final String ID = "id";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_ID = "event_id";
    public static final String EVENT_TIME = "event_time";
    public static final String PARTY_ID = "party_id";
    public static final String WALLET_ID = "wallet_id";

    private static RowMapper<WalletsMessage> messageRowMapper = (rs, i) -> buildWalletsMessage(
            rs.getLong(ID),
            rs.getString(EVENT_TYPE),
            rs.getString(PARTY_ID),
            rs.getString(WALLET_ID),
            rs.getString(EVENT_TIME),
            rs.getLong(EVENT_ID));

    public WalletsMessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public WalletsMessage getAny(String walletId) throws DaoException {
        WalletsMessage result = null;
        final String sql = "SELECT * FROM whook.wallets_message WHERE wallet_id =:wallet_id ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(WALLET_ID, walletId);
        try {
            result = getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.warn("WalletsMessage with id {} not exist!", walletId);
        } catch (NestedRuntimeException e) {
            throw new DaoException("WalletsMessageDaoImpl.getAny error with id " + walletId, e);
        }
        return result;
    }

    @Override
    @Transactional
    public void create(WalletsMessage message) throws DaoException {
        final String sql =
                "INSERT INTO whook.wallets_message (event_type, event_id,  event_time,  party_id,  wallet_id) " +
                "VALUES (CAST(:event_type as whook.eventtype), :event_id, :event_time, :party_id, :wallet_id) " +
                "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_TYPE, message.getEventType().toString())
                .addValue(EVENT_ID, message.getEvent().getEventID())
                .addValue(EVENT_TIME, TypeUtil.temporalToString(message.getEvent().getOccuredAt()))
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(WALLET_ID, message.getWalletId());
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            message.setId(keyHolder.getKey().longValue());
            log.info("WalletsMessage {} saved to db.", message);
            queueDao.createByMessageId(message.getId());
            taskDao.create(message.getId());
        } catch (NestedRuntimeException e) {
            throw new DaoException("Couldn't createByMessageId message with walletId "+ message.getWalletId(), e);
        }
    }

    @Override
    public Long getMaxEventId() {
        final String sql = "SELECT max(event_id) FROM whook.wallets_message";
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<>(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<WalletsMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM whook.wallets_message WHERE id in (:ids)";
        try {
            List<WalletsMessage> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            log.debug("messagesFromDb {}", messagesFromDb);
            return messagesFromDb;
        }  catch (NestedRuntimeException e) {
            throw new DaoException("WalletsMessageDaoImpl.getByIds error", e);
        }
    }
}
