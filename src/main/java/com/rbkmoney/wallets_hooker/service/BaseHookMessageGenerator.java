package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.GeneratorParam;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseHookMessageGenerator<T> implements HookMessageGenerator<T> {

    protected final Long parentIsNotExistId;

    @Override
    public final WebhookMessage generate(T event, WebHookModel model, GeneratorParam generatorParam) {
        if (generatorParam.getParentId() == null) {
            generatorParam.setParentId(parentIsNotExistId);
        }

        return generateMessage(event, model, generatorParam);
    }

    protected abstract WebhookMessage generateMessage(T event, WebHookModel model, GeneratorParam generatorParam);

}
