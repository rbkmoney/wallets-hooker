package com.rbkmoney.wallets.hooker.dao.webhook;

import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.domain.enums.EventType;
import com.rbkmoney.wallets.hooker.domain.tables.pojos.Webhook;

import java.util.List;

public interface WebHookDao {

    Webhook create(WebHookModel webhook);

    void delete(long id);

    WebHookModel getById(long id);

    List<WebHookModel> getModelByIdentityAndWalletId(String identityId, String walletId, EventType eventType);

    List<Webhook> getByIdentity(String identityId);

    List<WebHookModel> getByIdentityAndEventType(String identityId, EventType eventType);
}
