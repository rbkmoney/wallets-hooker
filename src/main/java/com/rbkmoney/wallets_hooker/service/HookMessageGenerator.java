package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.model.MessageGenParams;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface HookMessageGenerator<T> {

    WebhookMessage generate(T event, WebHookModel model, MessageGenParams messageGenParams);

}
