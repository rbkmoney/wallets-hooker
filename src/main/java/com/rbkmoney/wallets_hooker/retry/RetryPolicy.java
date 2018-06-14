package com.rbkmoney.wallets_hooker.retry;

import com.rbkmoney.wallets_hooker.model.Queue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RetryPolicy {
    boolean shouldDisable(Queue queue);
    RetryPolicyType getType();
    void fillQueue(Queue queue, ResultSet rs) throws SQLException;
    MapSqlParameterSource buildParamSource(Queue queue);
}
