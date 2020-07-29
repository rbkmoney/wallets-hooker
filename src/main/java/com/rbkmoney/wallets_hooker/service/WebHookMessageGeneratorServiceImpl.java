package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.model.MessageGenParams;
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
    protected WebhookMessage generateMessage(T event, WebHookModel model, MessageGenParams messageGenParams) {
        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        webhookMessage.setSourceId(messageGenParams.getSourceId());
        webhookMessage.setEventId(messageGenParams.getEventId());
        webhookMessage.setWebhookId(model.getId());
        webhookMessage.setParentEventId(messageGenParams.getParentId());
        webhookMessage.setUrl(model.getUrl());
        webhookMessage.setCreatedAt(messageGenParams.getCreatedAt());
        return webhookMessage;
    }
}
