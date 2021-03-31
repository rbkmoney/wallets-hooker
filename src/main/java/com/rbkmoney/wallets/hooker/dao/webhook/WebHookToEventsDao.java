package com.rbkmoney.wallets.hooker.dao.webhook;

import com.rbkmoney.wallets.hooker.domain.tables.pojos.WebhookToEvents;

import java.util.List;

public interface WebHookToEventsDao {

    void create(WebhookToEvents webhookToEvents);

    List<WebhookToEvents> get(long id);

}
