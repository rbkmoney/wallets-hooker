package com.rbkmoney.wallets.hooker.service;

import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.model.MessageGenParams;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseHookMessageGenerator<T> implements HookMessageGenerator<T> {

    protected final Long parentIsNotExistId;

    @Override
    public final WebhookMessage generate(T event, WebHookModel model, MessageGenParams messageGenParams) {
        if (messageGenParams.getParentId() == null) {
            messageGenParams.setParentId(parentIsNotExistId);
        }

        return generateMessage(event, model, messageGenParams);
    }

    protected abstract WebhookMessage generateMessage(T event, WebHookModel model, MessageGenParams messageGenParams);

}
