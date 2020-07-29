package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.fistful.account.Account;
import com.rbkmoney.fistful.base.*;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.fistful.wallet.AccountChange;
import com.rbkmoney.fistful.wallet.Event;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.fistful.withdrawal.CreatedChange;
import com.rbkmoney.fistful.withdrawal.EventSinkPayload;
import com.rbkmoney.fistful.withdrawal.StatusChange;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.fistful.withdrawal.status.Status;
import com.rbkmoney.fistful.withdrawal.status.Succeeded;
import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import org.apache.thrift.TBase;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;

public class TestBeanFactory {

    public static final String SOURCE_WALLET_ID = "sourceWalletId";
    public static final String IDENTITY_ID = "identityId";
    public static final String DESTINATION = "destination";
    public static final String WITHDRAWAL_ID = "withdrawalId";
    public static final long WALLET_ID = 21L;

    public static MachineEvent createDestination() {
        com.rbkmoney.fistful.destination.Change change = new com.rbkmoney.fistful.destination.Change();
        Destination destination = new Destination();
        destination.setName("name");
        destination.setExternalId("externalId");
        Resource resource = new Resource();
        BankCard bankCard = new BankCard();
        bankCard.setBin("1234");
        bankCard.setMaskedPan("421");
        bankCard.setPaymentSystem(BankCardPaymentSystem.mastercard);
        bankCard.setToken("token");
        resource.setBankCard(new ResourceBankCard(bankCard));
        destination.setResource(resource);
        change.setCreated(destination);

        TimestampedChange statusChanged = new TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                DESTINATION,
                1L,
                new ThriftSerializer<>(),
                statusChanged);
    }

    public static MachineEvent createDestinationAccount() {
        com.rbkmoney.fistful.destination.Change change = new com.rbkmoney.fistful.destination.Change();
        com.rbkmoney.fistful.destination.AccountChange accountChange = new com.rbkmoney.fistful.destination.AccountChange();
        Account account = new Account();
        account.setId("account");
        account.setCurrency(new CurrencyRef().setSymbolicCode("RUB"));
        account.setIdentity(IDENTITY_ID);
        accountChange.setCreated(account);
        change.setAccount(accountChange);

        TimestampedChange statusChanged = new TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                DESTINATION,
                2L,
                new ThriftSerializer<>(),
                statusChanged);
    }

    @NotNull
    public static com.rbkmoney.fistful.withdrawal.SinkEvent createWithdrawalEvent() {
        com.rbkmoney.fistful.withdrawal.Change withdrawalChange = new com.rbkmoney.fistful.withdrawal.Change();
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setDestinationId(DESTINATION);
        withdrawal.setExternalId("extId");
        withdrawal.setWalletId(SOURCE_WALLET_ID);
        withdrawal.setId(WITHDRAWAL_ID);

        Cash body = new Cash();
        body.setAmount(1000);
        CurrencyRef currency = new CurrencyRef();
        currency.setSymbolicCode("RUB");
        body.setCurrency(currency);
        withdrawal.setBody(body);
        withdrawalChange.setCreated(new CreatedChange()
                .setWithdrawal(withdrawal));

        com.rbkmoney.fistful.withdrawal.SinkEvent withdrawalSink = new com.rbkmoney.fistful.withdrawal.SinkEvent();
        EventSinkPayload eventSinkPayload = new EventSinkPayload();
        ArrayList<com.rbkmoney.fistful.withdrawal.Change> changesWithdrawal = new ArrayList<>();
        changesWithdrawal.add(withdrawalChange);
        eventSinkPayload.setChanges(changesWithdrawal);
        withdrawalSink.setPayload(eventSinkPayload);
        withdrawalSink.setSource(WITHDRAWAL_ID);
        withdrawalSink.setId(66L);
        withdrawalSink.setCreatedAt(Instant.now().toString());

        return withdrawalSink;
    }

    @NotNull
    public static SinkEvent createWalletEvent() {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setSource(SOURCE_WALLET_ID);
        Event payload = new Event();
        ArrayList<com.rbkmoney.fistful.wallet.Change> changes = new ArrayList<>();
        com.rbkmoney.fistful.wallet.Change change = new com.rbkmoney.fistful.wallet.Change();
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
        sinkEvent.setId(WALLET_ID);
        sinkEvent.setCreatedAt(Instant.now().toString());

        return sinkEvent;
    }

    @NotNull
    public static com.rbkmoney.fistful.withdrawal.SinkEvent createWithdrawalSucceeded() {
        com.rbkmoney.fistful.withdrawal.SinkEvent sinkEvent = new com.rbkmoney.fistful.withdrawal.SinkEvent();
        sinkEvent.setSource(TestBeanFactory.WITHDRAWAL_ID);
        EventSinkPayload payload = new EventSinkPayload();
        payload.setSequence(1);
        ArrayList<com.rbkmoney.fistful.withdrawal.Change> changes = new ArrayList<>();
        com.rbkmoney.fistful.withdrawal.Change change = new com.rbkmoney.fistful.withdrawal.Change();
        change.setStatusChanged(new StatusChange().setStatus(Status.succeeded(new Succeeded())));
        changes.add(change);

        payload.setChanges(changes);
        sinkEvent.setPayload(payload);
        sinkEvent.setId(67L);
        sinkEvent.setCreatedAt(Instant.now().toString());
        return sinkEvent;
    }

    public static WebHookModel createWebhookModel() {
        LinkedHashSet<EventType> eventTypes = new LinkedHashSet<>();
        eventTypes.add(EventType.WITHDRAWAL_CREATED);
        eventTypes.add(EventType.WITHDRAWAL_SUCCEEDED);
        return WebHookModel.builder()
                .enabled(true)
                .identityId(TestBeanFactory.IDENTITY_ID)
                .url("/qwe")
                .walletId(TestBeanFactory.SOURCE_WALLET_ID)
                .eventTypes(eventTypes)
                .build();
    }

    private static <T extends TBase> MachineEvent machineEvent(
            String sourceId,
            Long eventId,
            ThriftSerializer<T> depositChangeSerializer,
            T change) {
        return new MachineEvent()
                .setEventId(eventId)
                .setSourceId(sourceId)
                .setSourceNs("source_ns")
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setData(Value.bin(depositChangeSerializer.serialize("", change)));
    }
}
