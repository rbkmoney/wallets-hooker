package com.rbkmoney.wallets_hooker.kafka;


import com.rbkmoney.kafka.common.serialization.AbstractThriftDeserializer;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;

public class WebHookDeserializer extends AbstractThriftDeserializer<WebhookMessage> {

    @Override
    public WebhookMessage deserialize(String s, byte[] bytes) {
        return super.deserialize(bytes, new WebhookMessage());
    }

}
