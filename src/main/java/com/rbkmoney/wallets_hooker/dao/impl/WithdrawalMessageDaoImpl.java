package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
@DependsOn("dbInitializer")
public class WithdrawalMessageDaoImpl extends NamedParameterJdbcDaoSupport implements WithdrawalMessageDao {

    public static final String ID = "id";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_ID = "event_id";
    public static final String PARTY_ID = "party_id";
    public static final String OCCURED_AT = "occured_at";
    public static final String WITHDRAWAL_ID = "withdrawal_id";
    public static final String WITHDRAWAL_CREATED_AT = "withdrawal_created_at";
    public static final String WITHDRAWAL_WALLET_ID = "withdrawal_wallet_id";
    public static final String WITHDRAWAL_DESTINATION_ID = "withdrawal_destination_id";
    public static final String WITHDRAWAL_AMOUNT = "withdrawal_amount";
    public static final String WITHDRAWAL_CURRENCY_CODE = "withdrawal_currency_code";
    public static final String WITHDRAWAL_METADATA = "withdrawal_metadata";
    public static final String WITHDRAWAL_STATUS = "withdrawal_status";
    public static final String WITHDRAWAL_FAILURE_CODE = "withdrawal_failure_code";

    private static RowMapper<WithdrawalMessage> messageRowMapper = (rs, i) -> {
        WithdrawalMessage message = new WithdrawalMessage();
        message.setId(rs.getLong(ID));
        message.setEventType(EventType.valueOf(rs.getString(EVENT_TYPE)));
        message.setEventId(rs.getLong(EVENT_ID));
        message.setPartyId(rs.getString(PARTY_ID));
        message.setOccuredAt(rs.getString(OCCURED_AT));
        message.setWithdrawalId(rs.getString(WITHDRAWAL_ID));
        message.setWithdrawalCreatedAt(rs.getString(WITHDRAWAL_CREATED_AT));
        message.setWithdrawalWalletId(rs.getString(WITHDRAWAL_WALLET_ID));
        message.setWithdrawalDestinationId(rs.getString(WITHDRAWAL_DESTINATION_ID));
        message.setWithdrawalAmount(rs.getLong(WITHDRAWAL_AMOUNT));
        message.setWithdrawalCurrencyCode(rs.getString(WITHDRAWAL_CURRENCY_CODE));
        message.setWithdrawalMetadata(rs.getString(WITHDRAWAL_METADATA));
        message.setWithdrawalStatus(rs.getString(WITHDRAWAL_STATUS));
        message.setWithdrawalFailureCode(rs.getString(WITHDRAWAL_FAILURE_CODE));
        return message;
    };

    public WithdrawalMessageDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public WithdrawalMessage getAny(String withdrawalId) throws DaoException {
        final String sql = "SELECT * FROM whook.withdrawal_message WHERE withdrawal_id =:withdrawal_id ORDER BY id DESC LIMIT 1";
        MapSqlParameterSource params = new MapSqlParameterSource(WITHDRAWAL_ID, withdrawalId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, messageRowMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("WithdrawalMessage with withdrawalId " + withdrawalId + " not found");
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to get WithdrawalMessage with withdrawalId " + withdrawalId, e);
        }
    }

    @Override
    public Long create(WithdrawalMessage message) throws DaoException {
        final String sql =
                "INSERT INTO whook.withdrawal_message (event_type, event_id,  party_id, occured_at,  " +
                        "withdrawal_id, withdrawal_created_at, withdrawal_wallet_id, withdrawal_destination_id, " +
                        "withdrawal_amount, withdrawal_currency_code, withdrawal_metadata, withdrawal_status, withdrawal_failure_code) " +
                "VALUES (CAST(:event_type as whook.event_type), :event_id,  :party_id, :occured_at, " +
                        ":withdrawal_id, :withdrawal_created_at, :withdrawal_wallet_id, :withdrawal_destination_id, " +
                        ":withdrawal_amount, :withdrawal_currency_code, :withdrawal_metadata, :withdrawal_status, :withdrawal_failure_code) " +
                "RETURNING id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(EVENT_TYPE, message.getEventType().name())
                .addValue(EVENT_ID, message.getEventId())
                .addValue(PARTY_ID, message.getPartyId())
                .addValue(OCCURED_AT, message.getOccuredAt())
                .addValue(WITHDRAWAL_ID, message.getWithdrawalId())
                .addValue(WITHDRAWAL_CREATED_AT, message.getWithdrawalCreatedAt())
                .addValue(WITHDRAWAL_WALLET_ID, message.getWithdrawalWalletId())
                .addValue(WITHDRAWAL_DESTINATION_ID, message.getWithdrawalDestinationId())
                .addValue(WITHDRAWAL_AMOUNT, message.getWithdrawalAmount())
                .addValue(WITHDRAWAL_CURRENCY_CODE, message.getWithdrawalCurrencyCode())
                .addValue(WITHDRAWAL_METADATA, message.getWithdrawalMetadata())
                .addValue(WITHDRAWAL_STATUS, message.getWithdrawalStatus())
                .addValue(WITHDRAWAL_FAILURE_CODE, message.getWithdrawalFailureCode())
                ;
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            getNamedParameterJdbcTemplate().update(sql, params, keyHolder);
            return keyHolder.getKey().longValue();
        } catch (NestedRuntimeException e) {
            throw new DaoException("Error to create WithdrawalMessage with withdrawalId " + message.getWithdrawalId(), e);
        }
    }

    @Override
    public Long getLastEventId() {
        final String sql = "SELECT max(event_id) FROM whook.withdrawal_message";
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, new HashMap<>(), Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<WithdrawalMessage> getBy(Collection<Long> messageIds) throws DaoException {
        final String sql = "SELECT * FROM whook.withdrawal_message WHERE id in (:ids)";
        try {
            List<WithdrawalMessage> messagesFromDb = getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", messageIds), messageRowMapper);
            return messagesFromDb;
        }  catch (NestedRuntimeException e) {
            throw new DaoException("Error to get list of withdrawal messages by ids " + messageIds, e);
        }
    }
}
