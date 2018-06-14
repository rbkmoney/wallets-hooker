package com.rbkmoney.wallets_hooker.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.rbkmoney.damsel.webhooker.*;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.wallets_hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import com.rbkmoney.swag_wallets_webhook_events.Event;
import com.rbkmoney.swag_wallets_webhook_events.Withdrawal;
import com.rbkmoney.swag_wallets_webhook_events.WithdrawalWallet;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class ConverterUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static EventFilter convertEventFilter(Collection<WebhookAdditionalFilter> webhookAdditionalFilters) {
        if (webhookAdditionalFilters == null || webhookAdditionalFilters.isEmpty()) {
            return null;
        }
        EventFilter eventFilter = new EventFilter();
        WalletEventFilter walletEventFilter = new WalletEventFilter();
        Set<WalletEventType> eventTypes = new HashSet<>();
        walletEventFilter.setTypes(eventTypes);
        eventFilter.setWallet(walletEventFilter);
        for (WebhookAdditionalFilter webhookAdditionalFilter : webhookAdditionalFilters) {
            EventType eventTypeCode = webhookAdditionalFilter.getEventType();
            switch (eventTypeCode) {
                case WALLET_WITHDRAWAL_CREATED:
                    eventTypes.add(WalletEventType.withdrawal(WalletWithdrawalEventType.started(new WalletWithdrawalStarted())));
                    break;
                case WALLET_WITHDRAWAL_SUCCEEDED:
                    eventTypes.add(WalletEventType.withdrawal(WalletWithdrawalEventType.succeeded(new WalletWithdrawalSucceeded())));
                    break;
                case WALLET_WITHDRAWAL_FAILED:
                    eventTypes.add(WalletEventType.withdrawal(WalletWithdrawalEventType.failed(new WalletWithdrawalFailed())));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown event code "+eventTypeCode+"; must be one of these: "+Arrays.toString(EventType.values()));
            }
        }
        return eventFilter;
    }

    public static Set<WebhookAdditionalFilter> convertWebhookAdditionalFilter(EventFilter eventFilter){
        Set<WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        if (eventFilter.isSetWallet()) {
            Set<WalletEventType> walletEventTypes = eventFilter.getWallet().getTypes();
            for (WalletEventType walletEventType : walletEventTypes) {
                WebhookAdditionalFilter webhookAdditionalFilter = new WebhookAdditionalFilter();
                eventTypeCodeSet.add(webhookAdditionalFilter);
                if (walletEventType.isSetWithdrawal()) {
                    WalletWithdrawalEventType withdrawal = walletEventType.getWithdrawal();
                    if (withdrawal.isSetStarted()) {
                        webhookAdditionalFilter.setEventType(EventType.WALLET_WITHDRAWAL_CREATED);
                    } else if (withdrawal.isSetSucceeded()) {
                        webhookAdditionalFilter.setEventType(EventType.WALLET_WITHDRAWAL_SUCCEEDED);
                    } else if (withdrawal.isSetFailed()) {
                        webhookAdditionalFilter.setEventType(EventType.WALLET_WITHDRAWAL_FAILED);
                    }
                }
            }
        }
        return eventTypeCodeSet;
    }

    public static Event.EventTypeEnum convertSwagEventType(EventType eventType) {
        switch (eventType) {
            case WALLET_WITHDRAWAL_CREATED:
                return Event.EventTypeEnum.WALLETWITHDRAWALSTARTED;
            case WALLET_WITHDRAWAL_SUCCEEDED:
                return Event.EventTypeEnum.WALLETWITHDRAWALSUCCEEDED;
            case WALLET_WITHDRAWAL_FAILED:
                return Event.EventTypeEnum.WALLETWITHDRAWALFAILED;
            default:
                throw new UnsupportedOperationException("Unknown event type; must be one of these: " + Arrays.toString(Event.EventTypeEnum.values()));
        }
    }

    public static String convertTopic(EventFilter eventFilter) {
        if (eventFilter.isSetWallet()) {
            return Event.TopicEnum.WALLETSTOPIC.getValue();
        }
        throw new UnsupportedOperationException("Unknown topic; must be one of these: " + Arrays.toString(Event.TopicEnum.values()));
    }

    public static WalletsMessage buildWalletsMessage(String sEventType, String partyId, String walletId, String occuredAt, long eventId) {
        return buildWalletsMessage(null, sEventType, partyId, walletId, occuredAt, eventId);
    }

    public static WalletsMessage buildWalletsMessage(Long id, String sEventType, String partyId, String walletId, String occuredAt, long eventId) {
        WalletsMessage message = new WalletsMessage();
        message.setId(id);
        EventType eventType = EventType.valueOf(sEventType);
        message.setEventType(eventType);
        message.setPartyId(partyId);
        message.setWalletId(walletId);
        Event event;
        Withdrawal withdrawal = new Withdrawal().wallet(new WithdrawalWallet().id(walletId));
        Event.EventTypeEnum swagEventType = convertSwagEventType(eventType);
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
        message.setEvent(event);
        event.setOccuredAt(OffsetDateTime.ofInstant(TypeUtil.stringToInstant(occuredAt), ZoneOffset.UTC));
        event.setEventID((int) eventId);
        event.setTopic(Event.TopicEnum.WALLETSTOPIC);
        event.setEventType(swagEventType);
        return message;
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
        hook.setTopic(convertTopic(webhookParams.getEventFilter()));
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
