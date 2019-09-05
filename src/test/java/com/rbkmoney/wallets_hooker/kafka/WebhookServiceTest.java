package com.rbkmoney.wallets_hooker.kafka;

import com.rbkmoney.fistful.destination.SinkEvent;
import com.rbkmoney.fistful.webhooker.*;
import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.handler.poller.DestinationEventSinkHandler;
import com.rbkmoney.wallets_hooker.handler.poller.TestBeanFactory;
import com.rbkmoney.wallets_hooker.handler.poller.WalletEventSinkHandler;
import com.rbkmoney.wallets_hooker.handler.poller.WithdrawalEventSinkHandler;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
@TestPropertySource(properties = "merchant.callback.timeout=1")
public class WebhookServiceTest extends AbstractKafkaIntegrationTest {


    public static final String URL = "http://localhost:8089";
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEST = "/test";
    public static final String URL_2 = TEST + "/qwe";
    public static final String KEY = "key";

    @Value("${kafka.topic.hook}")
    private String topicName;

    @Autowired
    private WebhookManagerSrv.Iface requestHandler;

    @Autowired
    private WebHookMessageSenderService webHookMessageSenderService;

    @Autowired
    private DestinationEventSinkHandler destinationEventSinkHandler;

    @Autowired
    private WalletEventSinkHandler walletEventSinkHandler;

    @Autowired
    private WithdrawalEventSinkHandler withdrawalEventSinkHandler;

    @Test
    public void startTest() throws TException {
        WebhookParams webhookParams = new WebhookParams()
                .setEventFilter(new EventFilter()
                        .setTypes(Set.of(EventType.destination(DestinationEventType.created(new DestinationCreated())),
                                EventType.destination(DestinationEventType.authorized(new DestinationAuthorized())))))
                .setIdentityId(TestBeanFactory.IDENTITY_ID)
                .setUrl(TEST);
        Webhook webhook = requestHandler.create(webhookParams);

        Assert.assertEquals(TEST, webhook.getUrl());

        webhookParams.setUrl(URL_2);
        requestHandler.create(webhookParams);
        List<Webhook> list = requestHandler.getList(webhookParams.getIdentityId());
        Assert.assertEquals(2L, list.size());

        destinationEventSinkHandler.handle(TestBeanFactory.createDestination(), KEY);
        destinationEventSinkHandler.handle(TestBeanFactory.createDestinationAccount(), KEY);
        SinkEvent destinationAccount = TestBeanFactory.createDestinationAccount();
        destinationAccount.setSource(TestBeanFactory.DESTINATION + "_not");
        destinationEventSinkHandler.handle(destinationAccount, KEY);

        walletEventSinkHandler.handle(TestBeanFactory.createWalletEvent(), KEY);

        webhookParams = new WebhookParams()
                .setEventFilter(new EventFilter()
                        .setTypes(Set.of(EventType.withdrawal(WithdrawalEventType.started(new WithdrawalStarted())),
                                EventType.withdrawal(WithdrawalEventType.succeeded(new WithdrawalSucceeded())))))
                .setIdentityId(TestBeanFactory.IDENTITY_ID)
                .setWalletId(TestBeanFactory.SOURCE_WALLET_ID)
                .setUrl(TEST);
        webhook = requestHandler.create(webhookParams);

        withdrawalEventSinkHandler.handle(TestBeanFactory.createWithdrawalEvent(), KEY);
        withdrawalEventSinkHandler.handle(TestBeanFactory.createWithdrawalSucceeded(), KEY);

        Consumer<String, WebhookMessage> consumer = createConsumer(WebHookDeserializer.class);

        consumer.subscribe(List.of(topicName));
        ConsumerRecords<String, WebhookMessage> poll = consumer.poll(Duration.ofMillis(5000));
        Assert.assertEquals(4L, poll.count());

        Iterable<ConsumerRecord<String, WebhookMessage>> records = poll.records(topicName);

        ArrayList<WebhookMessage> webhookMessages = new ArrayList<>();
        records.forEach(consumerRecord -> webhookMessages.add(consumerRecord.value()));

        Assert.assertEquals(TestBeanFactory.DESTINATION, webhookMessages.get(0).source_id);
        Assert.assertEquals(TestBeanFactory.DESTINATION, webhookMessages.get(1).source_id);
        Assert.assertEquals(TestBeanFactory.WITHDRAWAL_ID, webhookMessages.get(2).source_id);
        Assert.assertEquals(TestBeanFactory.WITHDRAWAL_ID, webhookMessages.get(3).source_id);
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
