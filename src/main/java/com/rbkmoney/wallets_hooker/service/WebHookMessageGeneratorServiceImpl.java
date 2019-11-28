package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.GeneratorParam;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebHookMessageGeneratorServiceImpl<T> extends BaseHookMessageGenerator<T> {

    public WebHookMessageGeneratorServiceImpl(@Value("${parent.not.exist.id}") Long parentId) {
        super(parentId);
    }

    @Override
    protected WebhookMessage generateMessage(T event, WebHookModel model, GeneratorParam generatorParam) {
        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        webhookMessage.setSourceId(generatorParam.getSourceId());
        webhookMessage.setEventId(generatorParam.getEventId());
        webhookMessage.setWebhookId(model.getId());
        webhookMessage.setParentEventId(generatorParam.getParentId());
        webhookMessage.setUrl(model.getUrl());
        webhookMessage.setCreatedAt(generatorParam.getCreatedAt());
        return webhookMessage;
    }
}
