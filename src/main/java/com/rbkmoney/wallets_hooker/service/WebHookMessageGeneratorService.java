package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public interface WebHookMessageGeneratorService {

    <T> WebhookMessage generate(T event, WebHookModel model);

}
