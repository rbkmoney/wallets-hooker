package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.fistful.webhooker.DestinationEventType;
import com.rbkmoney.fistful.webhooker.WebhookParams;
import com.rbkmoney.fistful.webhooker.WithdrawalEventType;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.exception.UnknownEventTypeException;

import java.util.Set;
import java.util.stream.Collectors;

public class EventTypeUtils {

    public static Set<EventType> convertEventTypes(WebhookParams event) {
        return event.getEventFilter().getTypes().stream()
                .map(EventTypeUtils::resolveEventType)
                .collect(Collectors.toSet());
    }

    public static EventType resolveEventType(com.rbkmoney.fistful.webhooker.EventType type) {
        if (type.isSetWithdrawal()) {
            WithdrawalEventType withdrawal = type.getWithdrawal();
            if (withdrawal.isSetFailed()) {
                return EventType.WITHDRAWAL_FAILED;
            } else if (withdrawal.isSetStarted()) {
                return EventType.WITHDRAWAL_CREATED;
            } else if (withdrawal.isSetSucceeded()) {
                return EventType.WITHDRAWAL_CREATED;
            }
        } else if (type.isSetDestination()) {
            DestinationEventType destination = type.getDestination();
            if (destination.isSetCreated()) {
                return EventType.DESTINATION_CREATED;
            } else if (destination.isSetAuthorized()) {
                return EventType.DESTINATION_AUTHORIZED;
            } else if (destination.isSetUnauthorized()) {
                return EventType.DESTINATION_UNAUTHORIZED;
            }
        }
        throw new UnknownEventTypeException(type.toString());
    }

}
