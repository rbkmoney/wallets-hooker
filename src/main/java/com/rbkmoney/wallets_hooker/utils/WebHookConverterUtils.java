package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.fistful.webhooker.*;
import com.rbkmoney.wallets_hooker.exception.UnknownEventTypeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebHookConverterUtils {

    public static EventFilter generateEventFilter(Set<com.rbkmoney.wallets_hooker.domain.enums.EventType> eventTypes) {
        EventFilter eventFilter = new EventFilter();
        if (eventTypes != null) {
            eventFilter.setTypes(eventTypes.stream()
                    .map(WebHookConverterUtils::resolveEventType)
                    .collect(Collectors.toSet()));
        } else {
            eventFilter.setTypes(Collections.emptySet());
        }
        return eventFilter;
    }

    private static EventType resolveEventType(com.rbkmoney.wallets_hooker.domain.enums.EventType type) {
        switch (type) {
            case WITHDRAWAL_CREATED:
                return EventType.withdrawal(WithdrawalEventType.started(new WithdrawalStarted()));
            case WITHDRAWAL_FAILED:
                return EventType.withdrawal(WithdrawalEventType.failed(new WithdrawalFailed()));
            case WITHDRAWAL_SUCCEEDED:
                return EventType.withdrawal(WithdrawalEventType.succeeded(new WithdrawalSucceeded()));
            case DESTINATION_AUTHORIZED:
                return EventType.destination(DestinationEventType.authorized(new DestinationAuthorized()));
            case DESTINATION_UNAUTHORIZED:
                return EventType.destination(DestinationEventType.unauthorized(new DestinationUnauthorized()));
            case DESTINATION_CREATED:
                return EventType.destination(DestinationEventType.created(new DestinationCreated()));
            default:
                throw new UnknownEventTypeException();
        }
    }

}
