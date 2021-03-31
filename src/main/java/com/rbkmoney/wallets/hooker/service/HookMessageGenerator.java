package com.rbkmoney.wallets.hooker.service;

import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.model.MessageGenParams;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface HookMessageGenerator<T> {

    WebhookMessage generate(T event, WebHookModel model, MessageGenParams messageGenParams);

}
