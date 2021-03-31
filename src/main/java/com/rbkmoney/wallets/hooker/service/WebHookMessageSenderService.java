package com.rbkmoney.wallets.hooker.service;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface WebHookMessageSenderService {

    void send(WebhookMessage webhookMessage);

}
