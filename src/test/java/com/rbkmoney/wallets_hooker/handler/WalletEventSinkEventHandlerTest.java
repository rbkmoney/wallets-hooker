package com.rbkmoney.wallets_hooker.handler;

import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.wallets_hooker.service.kafka.DestinationEventService;
import com.rbkmoney.wallets_hooker.service.kafka.WalletEventService;
import com.rbkmoney.wallets_hooker.service.kafka.WithdrawalEventService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
@TestPropertySource(properties = "fistful.pollingEnabled=false")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WalletEventSinkEventHandlerTest extends AbstractPostgresIntegrationTest {

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
    public void handle() {
        WebHookModel webhook = TestBeanFactory.createWebhookModel();

        webHookDao.create(webhook);

        destinationEventService.handleEvents(List.of(TestBeanFactory.createDestination()));
        destinationEventService.handleEvents(List.of(TestBeanFactory.createDestinationAccount()));
        walletEventService.handleEvents(List.of(TestBeanFactory.createWalletEvent()));
        withdrawalEventService.handleEvents(List.of(TestBeanFactory.createWithdrawalEvent()));

        verify(webHookMessageSenderService, times(1))
                .send(any());

        withdrawalEventService.handleEvents(List.of(TestBeanFactory.createWithdrawalSucceeded()));
        verify(webHookMessageSenderService, times(2))
                .send(any());
    }
}