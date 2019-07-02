package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface HookMessageGenerator<T> {

    WebhookMessage generate(T event, WebHookModel model, String sourceId, Long eventId, String createdAt);

    WebhookMessage generate(T event, WebHookModel model, String sourceId, Long eventId, Long parentId, String createdAt);

}
