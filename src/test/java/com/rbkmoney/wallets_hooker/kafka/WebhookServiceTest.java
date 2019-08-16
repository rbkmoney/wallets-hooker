package com.rbkmoney.wallets_hooker.kafka;

import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
@TestPropertySource(properties = "merchant.callback.timeout=1")
public class WebhookServiceTest extends AbstractKafkaIntegrationTest {


    public static final String URL = "http://localhost:8089";
    public static final String APPLICATION_JSON = "application/json";


    @Autowired
    private WebHookMessageSenderService webHookMessageSenderService;

    @Test
    public void startTest() {
        webHookMessageSenderService.send(createWebhook("test_1",  Instant.now().toString(), 1));
    }

    @NotNull
    private WebhookMessage createWebhook(String sourceId, String createdAt, long eventId) {
        WebhookMessage webhook = new WebhookMessage();
        webhook.setSourceId(sourceId);
        webhook.setCreatedAt(createdAt);
        webhook.setUrl(URL);
        webhook.setContentType(APPLICATION_JSON);
        webhook.setRequestBody("\\{\\}".getBytes());
        webhook.setEventId(eventId);
        webhook.setAdditionalHeaders(new HashMap<>());
        webhook.setParentEventId(-1);
        return webhook;
    }
}
