package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface WebHookMessageSenderService {

    void send(WebhookMessage webhookMessage);

}
