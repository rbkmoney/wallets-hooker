package com.rbkmoney.wallets.hooker.service;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@ConditionalOnProperty(
        name = "webhook.sender.enabled",
        havingValue = "false")
public class DummyWebhookMessageSenderService implements WebHookMessageSenderService {

    @PostConstruct
    public void init() {
        log.warn("Webhook sending is disabled! Consider setting `webhook.sender.enabled` to `true`");
    }

    @Override
    public void send(WebhookMessage webhookMessage) {
        // do nothing
    }
}
