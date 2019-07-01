package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebHookMessageGeneratorServiceImpl<T> implements HookMessageGenerator<T> {

    @Value("${parent.not.exist.id}")
    private Long parentIsNotExistId;

    @Override
    public WebhookMessage generate(T event, WebHookModel model, Long eventId) {
        return generate(event, model, eventId, parentIsNotExistId);
    }

    @Override
    public WebhookMessage generate(T event, WebHookModel model, Long eventId, Long parentId) {
        WebhookMessage webhookMessage = new WebhookMessage();
        webhookMessage.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        webhookMessage.setEventId(eventId);
        webhookMessage.setWebhookId(model.getId());
        webhookMessage.setParentEventId(parentId);
        webhookMessage.setUrl(model.getUrl());
        return webhookMessage;
    }

}
