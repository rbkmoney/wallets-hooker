package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.fistful.account.Account;
import com.rbkmoney.fistful.base.BankCard;
import com.rbkmoney.fistful.base.BankCardPaymentSystem;
import com.rbkmoney.fistful.base.Cash;
import com.rbkmoney.fistful.base.CurrencyRef;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.Resource;
import com.rbkmoney.fistful.wallet.AccountChange;
import com.rbkmoney.fistful.wallet.Change;
import com.rbkmoney.fistful.wallet.Event;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.fistful.withdrawal.EventSinkPayload;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.fistful.withdrawal.WithdrawalStatus;
import com.rbkmoney.fistful.withdrawal.WithdrawalSucceeded;
import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
public class WalletEventSinkHandlerTest extends AbstractPostgresIntegrationTest {

    public static final String SOURCE_WALLET_ID = "sourceWalletId";
    public static final String IDENTITY_ID = "identityId";
    public static final String DESTINATION = "destination";
    public static final String WITHDRAWAL_ID = "withdrawalId";
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
    public void handle() {
        LinkedHashSet<EventType> eventTypes = new LinkedHashSet<>();
        eventTypes.add(EventType.WITHDRAWAL_CREATED);
        eventTypes.add(EventType.WITHDRAWAL_SUCCEEDED);
        WebHookModel webhook = WebHookModel.builder()
                .enabled(true)
                .identityId(IDENTITY_ID)
                .url("/qwe")
                .walletId(SOURCE_WALLET_ID)
                .eventTypes(eventTypes)
                .build();

        webHookDao.create(webhook);

        EventAction action = destinationEventSinkHandler.handle(createDestination(), "test");

        Assert.assertEquals(action, EventAction.CONTINUE);

        action = destinationEventSinkHandler.handle(createDestinationAccount(), "test");

        Assert.assertEquals(action, EventAction.CONTINUE);

        action = walletEventSinkHandler.handle(createWalletEvent(), "test");

        Assert.assertEquals(action, EventAction.CONTINUE);

        action = withdrawalEventSinkHandler.handle(createWithdrawalEvent(), "test");

        Assert.assertEquals(action, EventAction.CONTINUE);

        Mockito.verify(webHookMessageSenderService, Mockito.times(1))
                .send(any());

        com.rbkmoney.fistful.withdrawal.SinkEvent sinkEvent = new com.rbkmoney.fistful.withdrawal.SinkEvent();
        sinkEvent.setSource(WITHDRAWAL_ID);
        EventSinkPayload payload = new EventSinkPayload();
        payload.setSequence(1);
        ArrayList<com.rbkmoney.fistful.withdrawal.Change> changes = new ArrayList<>();
        com.rbkmoney.fistful.withdrawal.Change change = new com.rbkmoney.fistful.withdrawal.Change();
        WithdrawalSucceeded withdrawalSucceeded = new WithdrawalSucceeded();
        WithdrawalStatus succeeded = WithdrawalStatus.succeeded(withdrawalSucceeded);
        change.setStatusChanged(succeeded);
        changes.add(change);
        payload.setChanges(changes);
        sinkEvent.setPayload(payload);
        action = withdrawalEventSinkHandler.handle(sinkEvent, "test");

        Assert.assertEquals(action, EventAction.CONTINUE);

        Mockito.verify(webHookMessageSenderService, Mockito.times(2))
                .send(any());
    }

    @NotNull
    private com.rbkmoney.fistful.destination.SinkEvent createDestination() {
        com.rbkmoney.fistful.destination.SinkEvent sinkEvent = new com.rbkmoney.fistful.destination.SinkEvent();
        com.rbkmoney.fistful.destination.Event payload = new com.rbkmoney.fistful.destination.Event();
        ArrayList<com.rbkmoney.fistful.destination.Change> changes = new ArrayList<>();
        com.rbkmoney.fistful.destination.Change change = new com.rbkmoney.fistful.destination.Change();
        Destination destination = new Destination();
        destination.setId(DESTINATION);
        Resource resource = new Resource();
        BankCard bankCard = new BankCard();
        bankCard.setBin("1234");
        bankCard.setMaskedPan("421");
        bankCard.setPaymentSystem(BankCardPaymentSystem.mastercard);
        resource.setBankCard(bankCard);
        destination.setResource(resource);
        change.setCreated(destination);
        changes.add(change);
        payload.setChanges(changes);
        payload.setSequence(1);
        sinkEvent.setPayload(payload);
        sinkEvent.setSource(DESTINATION);
        return sinkEvent;
    }

    @NotNull
    private com.rbkmoney.fistful.destination.SinkEvent createDestinationAccount() {
        com.rbkmoney.fistful.destination.SinkEvent sinkEvent = new com.rbkmoney.fistful.destination.SinkEvent();
        com.rbkmoney.fistful.destination.Event payload = new com.rbkmoney.fistful.destination.Event();
        ArrayList<com.rbkmoney.fistful.destination.Change> changes = new ArrayList<>();
        com.rbkmoney.fistful.destination.Change change = new com.rbkmoney.fistful.destination.Change();
        Destination destination = new Destination();
        destination.setId(DESTINATION);
        com.rbkmoney.fistful.destination.AccountChange accountChange = new com.rbkmoney.fistful.destination.AccountChange();
        Account account = new Account();
        account.setIdentity(IDENTITY_ID);
        accountChange.setCreated(account);
        change.setAccount(accountChange);
        changes.add(change);
        payload.setChanges(changes);
        payload.setSequence(1);
        sinkEvent.setPayload(payload);
        sinkEvent.setSource(DESTINATION);
        return sinkEvent;
    }

    @NotNull
    private com.rbkmoney.fistful.withdrawal.SinkEvent createWithdrawalEvent() {
        com.rbkmoney.fistful.withdrawal.Change withdrawalChange = new com.rbkmoney.fistful.withdrawal.Change();
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setDestination(DESTINATION);
        withdrawal.setExternalId("extId");
        withdrawal.setSource(SOURCE_WALLET_ID);
        withdrawal.setId(WITHDRAWAL_ID);
        Cash body = new Cash();
        body.setAmount(1000);
        CurrencyRef currency = new CurrencyRef();
        currency.setSymbolicCode("RUB");
        body.setCurrency(currency);
        withdrawal.setBody(body);
        withdrawalChange.setCreated(withdrawal);

        com.rbkmoney.fistful.withdrawal.SinkEvent withdrawalSink = new com.rbkmoney.fistful.withdrawal.SinkEvent();
        EventSinkPayload eventSinkPayload = new EventSinkPayload();
        ArrayList<com.rbkmoney.fistful.withdrawal.Change> changesWithdrawal = new ArrayList<>();
        changesWithdrawal.add(withdrawalChange);
        eventSinkPayload.setChanges(changesWithdrawal);
        withdrawalSink.setPayload(eventSinkPayload);
        withdrawalSink.setSource(SOURCE_WALLET_ID);
        return withdrawalSink;
    }

    @NotNull
    private SinkEvent createWalletEvent() {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setSource(SOURCE_WALLET_ID);
        Event payload = new Event();
        ArrayList<Change> changes = new ArrayList<>();
        Change change = new Change();
        AccountChange accountChange = new AccountChange();
        Account account = new Account();
        account.setId("accountId");
        CurrencyRef currency = new CurrencyRef();
        currency.setSymbolicCode("RUB");
        account.setIdentity(IDENTITY_ID);
        account.setCurrency(currency);
        accountChange.setCreated(account);
        change.setAccount(accountChange);
        changes.add(change);
        payload.setChanges(changes);
        payload.setSequence(1);
        sinkEvent.setPayload(payload);
        return sinkEvent;
    }
}