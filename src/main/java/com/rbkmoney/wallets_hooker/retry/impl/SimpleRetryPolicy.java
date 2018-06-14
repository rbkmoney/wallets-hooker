package com.rbkmoney.wallets_hooker.retry.impl;

import com.rbkmoney.wallets_hooker.model.Queue;
import com.rbkmoney.wallets_hooker.retry.RetryPolicy;
import com.rbkmoney.wallets_hooker.retry.RetryPolicyType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SimpleRetryPolicy implements RetryPolicy {

    private long[] delays = {30, 300, 900, 3600,
            3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600,
            3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600,
            3600, 3600, 3600, 3600
    }; //in seconds

    @Override
    public RetryPolicyType getType() {
        return RetryPolicyType.SIMPLE;
    }

    @Override
    public boolean shouldDisable(Queue queue) {
        queue.setFailCount(queue.getFailCount() + 1);
        queue.setLastFailTime(System.currentTimeMillis());
        boolean isFail = queue.getFailCount() > delays.length;
        if (!isFail) {
            queue.setNextTime(queue.getLastFailTime() + (delays[queue.getFailCount() - 1] * 1000));
        }
        return isFail;
    }

    @Override
    public void fillQueue(Queue queue, ResultSet rs) throws SQLException {
        queue.setFailCount(rs.getInt("fail_count"));
        queue.setLastFailTime(rs.getLong("last_fail_time"));
        queue.setNextTime(rs.getLong("next_time"));
    }

    @Override
    public MapSqlParameterSource buildParamSource(Queue queue) {
        return new MapSqlParameterSource("id", queue.getId())
                .addValue("fail_count", queue.getFailCount())
                .addValue("last_fail_time", queue.getLastFailTime())
                .addValue("next_time", queue.getNextTime());
    }
}
