package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface HookMessageGenerator<T> {

    WebhookMessage generate(T event, WebHookModel model, Long eventId);

    WebhookMessage generate(T event, WebHookModel model, Long eventId, Long parentId);

}
