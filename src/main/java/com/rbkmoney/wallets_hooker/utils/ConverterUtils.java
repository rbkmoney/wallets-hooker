package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.fistful.webhooker.*;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.model.MessageType;

import java.util.*;
import java.util.stream.Collectors;

public class ConverterUtils {

    public static EventFilter convertEventFilter(Collection<Hook.WebhookAdditionalFilter> webhookAdditionalFilters) {
        if (webhookAdditionalFilters == null || webhookAdditionalFilters.isEmpty()) {
            return null;
        }
        EventFilter eventFilter = new EventFilter();
        Set<com.rbkmoney.fistful.webhooker.EventType> eventTypes = new HashSet<>();
        eventFilter.setTypes(eventTypes);
        for (Hook.WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
            EventType eventTypeCode = webhookAdditionalFilter.getEventType();
            if (webhookAdditionalFilter.getMessageType() == MessageType.WITHDRAWAL) {
                switch (eventTypeCode) {
                    case WITHDRAWAL_CREATED:
                        eventTypes.add(com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.started(new WithdrawalStarted())));
                        break;
                    case WITHDRAWAL_SUCCEEDED:
                        eventTypes.add(com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.succeeded(new WithdrawalSucceeded())));
                        break;
                    case WITHDRAWAL_FAILED:
                        eventTypes.add(com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.failed(new WithdrawalFailed())));
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown event code " + eventTypeCode + "; must be one of these: " + Arrays.toString(EventType.values()));
                }
            }
        }
        return eventFilter;
    }

    public static Set<Hook.WebhookAdditionalFilter> convertWebhookAdditionalFilter(EventFilter eventFilter){
        Set<Hook.WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        Set<com.rbkmoney.fistful.webhooker.EventType> eventTypes = eventFilter.getTypes();
        for (com.rbkmoney.fistful.webhooker.EventType eventType : eventTypes) {
            Hook.WebhookAdditionalFilter webhookAdditionalFilter = new Hook.WebhookAdditionalFilter();
            if (eventType.isSetWithdrawal()) {
                WithdrawalEventType withdrawal = eventType.getWithdrawal();
                if (withdrawal.isSetStarted()) {
                    webhookAdditionalFilter.setEventType(EventType.WITHDRAWAL_CREATED);
                } else if (withdrawal.isSetSucceeded()) {
                    webhookAdditionalFilter.setEventType(EventType.WITHDRAWAL_SUCCEEDED);
                } else if (withdrawal.isSetFailed()) {
                    webhookAdditionalFilter.setEventType(EventType.WITHDRAWAL_FAILED);
                }
                webhookAdditionalFilter.setMessageType(MessageType.WITHDRAWAL);
            }
            eventTypeCodeSet.add(webhookAdditionalFilter);
        }
        return eventTypeCodeSet;
    }


    public static Webhook convertHook(Hook hook){
        return new Webhook(
                hook.getId(),
                hook.getPartyId(),
                convertEventFilter(hook.getFilters()),
                hook.getUrl(),
                buildFormattedPubKey(hook.getPubKey()),
                hook.isEnabled());
    }

    public static Hook convertHook(WebhookParams webhookParams){
        Hook hook = new Hook();
        hook.setPartyId(webhookParams.getPartyId());
        hook.setUrl(webhookParams.getUrl());
        hook.setFilters(convertWebhookAdditionalFilter(webhookParams.getEventFilter()));

        return hook;
    }

    public static List<Webhook> convertHooks(List<Hook> hooks){
        return hooks.stream().map(h -> convertHook(h)).collect(Collectors.toList());
    }

    private static String buildFormattedPubKey(String key) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                key.replaceAll("(.{64})", "$1\n") +
                "\n-----END PUBLIC KEY-----";
    }
}
