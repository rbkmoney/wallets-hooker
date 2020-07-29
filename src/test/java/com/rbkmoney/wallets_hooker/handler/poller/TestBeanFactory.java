package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.fistful.account.Account;
import com.rbkmoney.fistful.base.*;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.fistful.wallet.AccountChange;
import com.rbkmoney.fistful.withdrawal.CreatedChange;
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

        TimestampedChange timestampedChange = new TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                DESTINATION,
                1L,
                new ThriftSerializer<>(),
                timestampedChange);
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

        TimestampedChange timestampedChange = new TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                DESTINATION,
                2L,
                new ThriftSerializer<>(),
                timestampedChange);
    }

    public static MachineEvent createWithdrawalEvent() {
        com.rbkmoney.fistful.withdrawal.Change change = new com.rbkmoney.fistful.withdrawal.Change();
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
        change.setCreated(new CreatedChange()
                .setWithdrawal(withdrawal));

        com.rbkmoney.fistful.withdrawal.TimestampedChange timestampedChange = new com.rbkmoney.fistful.withdrawal.TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                WITHDRAWAL_ID,
                66L,
                new ThriftSerializer<>(),
                timestampedChange);
    }

    public static MachineEvent createWalletEvent() {
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

        com.rbkmoney.fistful.wallet.TimestampedChange timestampedChange = new com.rbkmoney.fistful.wallet.TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                SOURCE_WALLET_ID,
                WALLET_ID,
                new ThriftSerializer<>(),
                timestampedChange);
    }

    public static MachineEvent createWithdrawalSucceeded() {
        com.rbkmoney.fistful.withdrawal.Change change = new com.rbkmoney.fistful.withdrawal.Change();
        change.setStatusChanged(new StatusChange().setStatus(Status.succeeded(new Succeeded())));

        com.rbkmoney.fistful.withdrawal.TimestampedChange timestampedChange = new com.rbkmoney.fistful.withdrawal.TimestampedChange()
                .setOccuredAt("2016-03-22T06:12:27Z")
                .setChange(change);

        return machineEvent(
                WITHDRAWAL_ID,
                67L,
                new ThriftSerializer<>(),
                timestampedChange);
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
