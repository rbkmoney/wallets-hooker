package com.rbkmoney.wallets_hooker.kafka;

import com.rbkmoney.fistful.webhooker.*;
import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.handler.poller.TestBeanFactory;
import com.rbkmoney.wallets_hooker.handler.poller.WalletEventSinkHandler;
import com.rbkmoney.wallets_hooker.handler.poller.WithdrawalEventSinkHandler;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.wallets_hooker.service.kafka.DestinationEventService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
@TestPropertySource(properties = "merchant.callback.timeout=1")
public class WebhookServiceTest extends AbstractKafkaIntegrationTest {

    private static final String TEST = "/test";
    private static final String URL_2 = TEST + "/qwe";
    private static final String KEY = "key";

    @Value("${kafka.topic.hook.name}")
    private String topicName;

    @Autowired
    private WebhookManagerSrv.Iface requestHandler;

    @Autowired
    private WebHookMessageSenderService webHookMessageSenderService;

    @Autowired
    private DestinationEventService destinationEventService;

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

        ThriftSerializer<Webhook> webhookThriftSerializer = new ThriftSerializer<>();

        byte[] serialize = webhookThriftSerializer.serialize("t", webhook);
        assertTrue(serialize.length > 0);
        assertEquals(TEST, webhook.getUrl());

        webhookParams.setUrl(URL_2);
        requestHandler.create(webhookParams);
        List<Webhook> list = requestHandler.getList(webhookParams.getIdentityId());
        assertEquals(2L, list.size());

        destinationEventService.handleEvents(List.of(TestBeanFactory.createDestination()));
        destinationEventService.handleEvents(List.of(TestBeanFactory.createDestinationAccount()));
        MachineEvent destinationAccount = TestBeanFactory.createDestinationAccount();
        destinationAccount.setSourceId(TestBeanFactory.DESTINATION + "_not");
        destinationEventService.handleEvents(List.of(destinationAccount));

        walletEventSinkHandler.handle(TestBeanFactory.createWalletEvent(), KEY);

        webhookParams = new WebhookParams()
                .setEventFilter(new EventFilter()
                        .setTypes(Set.of(EventType.withdrawal(WithdrawalEventType.started(new WithdrawalStarted())),
                                EventType.withdrawal(WithdrawalEventType.succeeded(new WithdrawalSucceeded())))))
                .setIdentityId(TestBeanFactory.IDENTITY_ID)
                .setWalletId(TestBeanFactory.SOURCE_WALLET_ID)
                .setUrl(TEST);
        requestHandler.create(webhookParams);

        withdrawalEventSinkHandler.handle(TestBeanFactory.createWithdrawalEvent(), KEY);
        withdrawalEventSinkHandler.handle(TestBeanFactory.createWithdrawalSucceeded(), KEY);

        Consumer<String, WebhookMessage> consumer = createConsumer(WebHookDeserializer.class);

        consumer.subscribe(List.of(topicName));
        ConsumerRecords<String, WebhookMessage> poll = consumer.poll(Duration.ofMillis(5000));
        Iterable<ConsumerRecord<String, WebhookMessage>> records = poll.records(topicName);
        records.forEach(System.out::println);

        assertEquals(4L, poll.count());

        ArrayList<WebhookMessage> webhookMessages = new ArrayList<>();
        records.forEach(consumerRecord -> webhookMessages.add(consumerRecord.value()));

        assertEquals(TestBeanFactory.DESTINATION, webhookMessages.get(0).source_id);
        assertEquals(TestBeanFactory.DESTINATION, webhookMessages.get(1).source_id);
        assertEquals(TestBeanFactory.WITHDRAWAL_ID, webhookMessages.get(2).source_id);
        assertEquals(TestBeanFactory.WITHDRAWAL_ID, webhookMessages.get(3).source_id);
    }
}
