package com.rbkmoney.wallets_hooker.dao.webhook;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;

import java.util.List;

public interface WebHookDao {

    Webhook create(WebHookModel webhook);

    void delete(long id);

    WebHookModel getById(long id);

    List<Webhook> getByIdentityAndWalletId(String identityId, String walletId, EventType eventType);

    List<WebHookModel> getModelByIdentityAndWalletId(String identityId, String walletId, EventType eventType);

    List<Webhook> getByIdentity(String identityId);
}
