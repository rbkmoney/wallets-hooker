package com.rbkmoney.wallets.hooker.handler;

import com.rbkmoney.wallets.hooker.WalletsHookerApplication;
import com.rbkmoney.wallets.hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.service.WebHookMessageSenderService;
import com.rbkmoney.wallets.hooker.service.kafka.DestinationEventService;
import com.rbkmoney.wallets.hooker.service.kafka.WalletEventService;
import com.rbkmoney.wallets.hooker.service.kafka.WithdrawalEventService;
import com.rbkmoney.wallets.hooker.dao.AbstractPostgresIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WalletsHookerApplication.class)
@TestPropertySource(properties = "fistful.pollingEnabled=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingWithdrawalReferenceEventHandlerTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private WalletEventService walletEventService;

    @Autowired
    private WithdrawalEventService withdrawalEventService;

    @Autowired
    private DestinationEventService destinationEventService;

    @Autowired
    private WebHookDao webHookDao;

    @MockBean
    private WebHookMessageSenderService webHookMessageSenderService;

    @Test
    public void handleWaitingWithdrawalReference() throws InterruptedException {
        WebHookModel webhook = TestBeanFactory.createWebhookModel();
        webHookDao.create(webhook);

        destinationEventService.handleEvents(List.of(TestBeanFactory.createDestination()));

        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            withdrawalEventService.handleEvents(List.of(TestBeanFactory.createWithdrawalEvent()));
            verify(webHookMessageSenderService, times(1))
                    .send(any());
            latch.countDown();
        }).start();

        destinationEventService.handleEvents(List.of(TestBeanFactory.createDestinationAccount()));
        walletEventService.handleEvents(List.of(TestBeanFactory.createWalletEvent()));

        latch.await();
    }
}
