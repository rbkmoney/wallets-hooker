package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
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

import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
@TestPropertySource(properties = "fistful.pollingEnabled=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingWithdrawalReferenceHandlerTest extends AbstractPostgresIntegrationTest {

    @Autowired
    WalletEventSinkHandler walletEventSinkHandler;
    @Autowired
    WithdrawalEventSinkHandler withdrawalEventSinkHandler;
    @Autowired
    DestinationEventSinkHandler destinationEventSinkHandler;
    @Autowired
    WebHookDao webHookDao;

    @MockBean
    WebHookMessageSenderService webHookMessageSenderService;

    @Test
    public void handleWaitingWithdrawalReference() throws InterruptedException {
        WebHookModel webhook = TestBeanFactory.createWebhookModel();
        webHookDao.create(webhook);

        EventAction action = destinationEventSinkHandler.handle(TestBeanFactory.createDestination(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            EventAction actionNew = withdrawalEventSinkHandler.handle(TestBeanFactory.createWithdrawalEvent(), "test");
            Assert.assertEquals(actionNew, EventAction.CONTINUE);
            Mockito.verify(webHookMessageSenderService, Mockito.times(1))
                    .send(any());
            latch.countDown();
        }).start();

        action = destinationEventSinkHandler.handle(TestBeanFactory.createDestinationAccount(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        action = walletEventSinkHandler.handle(TestBeanFactory.createWalletEvent(), "test");
        Assert.assertEquals(action, EventAction.CONTINUE);

        latch.await();
    }

}