package com.rbkmoney.wallets.hooker.dao.webhook.mapper;

import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.rbkmoney.wallets.hooker.domain.tables.IdentityKey.IDENTITY_KEY;
import static com.rbkmoney.wallets.hooker.domain.tables.Webhook.WEBHOOK;

public class WebHookModelRowMapper implements RowMapper<WebHookModel> {

    @Override
    public WebHookModel mapRow(ResultSet rs, int i) throws SQLException {
        return WebHookModel.builder()
                .id(rs.getLong(WEBHOOK.ID.getName()))
                .identityId(rs.getString(WEBHOOK.IDENTITY_ID.getName()))
                .walletId(rs.getString(WEBHOOK.WALLET_ID.getName()))
                .eventTypes(null)
                .url(rs.getString(WEBHOOK.URL.getName()))
                .enabled(rs.getBoolean(WEBHOOK.ENABLED.getName()))
                .pubKey(rs.getString(IDENTITY_KEY.PUB_KEY.getName()))
                .privateKey(rs.getString(IDENTITY_KEY.PRIV_KEY.getName()))
                .build();
    }
}
