package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.constant.EventTopic;
import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.EventLogDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
@TestPropertySource(properties = "fistful.pollingEnabled=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WalletEventSinkHandlerTest extends AbstractPostgresIntegrationTest {

    @Autowired
    WalletEventSinkHandler walletEventSinkHandler;
    @Autowired
    WithdrawalEventSinkHandler withdrawalEventSinkHandler;
    @Autowired
    DestinationEventSinkHandler destinationEventSinkHandler;
    @Autowired
    WebHookDao webHookDao;
    @Autowired
    EventLogDao eventLogDao;

    @MockBean
    WebHookMessageSenderService webHookMessageSenderService;

    @Test
    public void handle() {
        WebHookModel webhook = TestBeanFactory.createWebhookModel();

        webHookDao.create(webhook);

        EventAction action = destinationEventSinkHandler.handle(TestBeanFactory.createDestination(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        action = destinationEventSinkHandler.handle(TestBeanFactory.createDestinationAccount(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        action = walletEventSinkHandler.handle(TestBeanFactory.createWalletEvent(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        action = withdrawalEventSinkHandler.handle(TestBeanFactory.createWithdrawalEvent(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        Mockito.verify(webHookMessageSenderService, Mockito.times(1))
                .send(any());

        SinkEvent sinkEvent = TestBeanFactory.createWithdrawalSucceeded();

        action = withdrawalEventSinkHandler.handle(sinkEvent, "test");
        Assert.assertEquals(action, EventAction.CONTINUE);
        Mockito.verify(webHookMessageSenderService, Mockito.times(2))
                .send(any());

        Long lastEventId = eventLogDao.getLastEventId(EventTopic.DESTINATION, 0L);
        Assert.assertEquals(2L, lastEventId.longValue());

        lastEventId = eventLogDao.getLastEventId(EventTopic.WALLET, 0L);
        Assert.assertEquals(TestBeanFactory.WALLET_ID, lastEventId.longValue());

        lastEventId = eventLogDao.getLastEventId(EventTopic.WITHDRAWAL, 0L);
        Assert.assertEquals(67L, lastEventId.longValue());
    }

}