package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.http.entity.ContentType;

public interface HookMessageGenerator<T> {

    WebhookMessage generate(T event, WebHookModel model, Long eventId, Long parentId);

}
