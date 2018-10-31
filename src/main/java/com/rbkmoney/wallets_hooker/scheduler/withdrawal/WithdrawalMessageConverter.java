package com.rbkmoney.wallets_hooker.scheduler.withdrawal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.swag_wallets_webhook_events.Event;
import com.rbkmoney.swag_wallets_webhook_events.Withdrawal;
import com.rbkmoney.swag_wallets_webhook_events.WithdrawalBody;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import com.rbkmoney.wallets_hooker.scheduler.MessageConverter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

@Component
public class WithdrawalMessageConverter extends MessageConverter<WithdrawalMessage> {
    @Override
    public String convertToJson(WithdrawalMessage message) {
        Event event;
        Withdrawal withdrawal = new Withdrawal()
                .id(message.getWithdrawalId())
                .createdAt(OffsetDateTime.ofInstant(TypeUtil.stringToInstant(message.getWithdrawalCreatedAt()), ZoneOffset.UTC))
                .wallet(message.getWithdrawalWalletId())
                .destination(message.getWithdrawalDestinationId())
                .body(new WithdrawalBody()
                        .amount(message.getWithdrawalAmount())
                        .currency(message.getWithdrawalCurrencyCode()))
                .status(Withdrawal.StatusEnum.valueOf(message.getWithdrawalStatus()));

        Event.EventTypeEnum swagEventType = convertSwagEventType(message.getEventType());
        switch (swagEventType) {
            case WALLETWITHDRAWALSTARTED:
                event = new com.rbkmoney.swag_wallets_webhook_events.WalletWithdrawalStarted().withdrawal(withdrawal);
                break;
            case WALLETWITHDRAWALSUCCEEDED:
                event = new com.rbkmoney.swag_wallets_webhook_events.WalletWithdrawalSucceeded().withdrawal(withdrawal);
                break;
            case WALLETWITHDRAWALFAILED:
                event = new com.rbkmoney.swag_wallets_webhook_events.WalletWithdrawalFailed().withdrawal(withdrawal);
                break;
            default:
                throw new UnsupportedOperationException("Unknown event; must be one of these: " + Arrays.toString(Event.EventTypeEnum.values()));
        }
        event.setOccuredAt(OffsetDateTime.ofInstant(TypeUtil.stringToInstant(message.getOccuredAt()), ZoneOffset.UTC));
        event.setEventID((int) message.getEventId().longValue());
        event.setTopic(Event.TopicEnum.WALLETSTOPIC);
        event.setEventType(swagEventType);
        try {
            return getObjectMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Event.EventTypeEnum convertSwagEventType(EventType eventType) {
        switch (eventType) {
            case WITHDRAWAL_CREATED:
                return Event.EventTypeEnum.WALLETWITHDRAWALSTARTED;
            case WITHDRAWAL_SUCCEEDED:
                return Event.EventTypeEnum.WALLETWITHDRAWALSUCCEEDED;
            case WITHDRAWAL_FAILED:
                return Event.EventTypeEnum.WALLETWITHDRAWALFAILED;
            default:
                throw new UnsupportedOperationException("Unknown event type " + eventType + "; must be one of these: " + Arrays.toString(Event.EventTypeEnum.values()));
        }
    }
}
