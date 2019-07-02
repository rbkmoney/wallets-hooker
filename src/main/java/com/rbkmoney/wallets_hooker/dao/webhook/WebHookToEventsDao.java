package com.rbkmoney.wallets_hooker.dao.webhook;

import com.rbkmoney.wallets_hooker.domain.tables.pojos.WebhookToEvents;

import java.util.List;

public interface WebHookToEventsDao {

    void create(WebhookToEvents webhookToEvents);

    List<WebhookToEvents> get(long id);

}
