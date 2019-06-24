package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.fistful.webhooker.*;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.exception.UnknownEventTypeException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebHookModelToWebHookConverter implements Converter<WebHookModel, Webhook> {

    @Override
    public Webhook convert(WebHookModel event) {
        Webhook webHook = new Webhook();
        webHook.setId(event.getId());
        webHook.setEnabled(event.getEnabled());
        webHook.setIdentityId(event.getIdentityId());
        webHook.setWalletId(event.getWalletId());
        webHook.setPubKey(event.getPubKey());
        webHook.setEventFilter(generateEventFilter(event));
        return webHook;
    }

    private EventFilter generateEventFilter(WebHookModel event) {
        EventFilter eventFilter = new EventFilter();
        eventFilter.setTypes(event.getEventTypes().stream()
                .map(this::resolveEventType)
                .collect(Collectors.toSet()));
        return eventFilter;
    }

    private EventType resolveEventType(com.rbkmoney.wallets_hooker.domain.enums.EventType type) {
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