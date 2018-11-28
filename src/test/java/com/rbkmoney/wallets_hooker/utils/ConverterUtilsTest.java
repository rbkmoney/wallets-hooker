package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.fistful.webhooker.*;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.model.MessageType;
import org.junit.Test;

import java.util.*;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConverterUtilsTest {

    @Test
    public void convertEventFilter() {
        EventFilter eventFilter = ConverterUtils.convertEventFilter(Arrays.asList(
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL),
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL),
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL)
        ));
        assertTrue(eventFilter.getTypes().contains(com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.started(new WithdrawalStarted()))));
        assertTrue(eventFilter.getTypes().contains(com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.succeeded(new WithdrawalSucceeded()))));
        assertTrue(eventFilter.getTypes().contains(com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.failed(new WithdrawalFailed()))));
    }

    @Test
    public void convertWebhookAdditionalFilter() {
        Set<Hook.WebhookAdditionalFilter> webhookAdditionalFilters = ConverterUtils.convertWebhookAdditionalFilter(new EventFilter().setTypes(new HashSet<>(Arrays.asList(
                com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.started(new WithdrawalStarted())),
                com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.succeeded(new WithdrawalSucceeded())),
                com.rbkmoney.fistful.webhooker.EventType.withdrawal(WithdrawalEventType.failed(new WithdrawalFailed()))))));

        assertTrue(webhookAdditionalFilters.contains(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL)));
        assertTrue(webhookAdditionalFilters.contains(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL)));
        assertTrue(webhookAdditionalFilters.contains(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL)));
    }

    @Test
    public void convertHook() {
        Hook hook = random(Hook.class);
        List<EventType> eventTypesList = Arrays.asList(EventType.WITHDRAWAL_CREATED, EventType.WITHDRAWAL_SUCCEEDED, EventType.WITHDRAWAL_FAILED);
        hook.getFilters().forEach(f -> {
            f.setMessageType(MessageType.WITHDRAWAL);
            f.setEventType(eventTypesList.get(new Random().nextInt(eventTypesList.size())));
        });
        Webhook webhook = ConverterUtils.convertHook(hook);
        assertEquals(hook.getPartyId(), webhook.getPartyId());
        assertEquals(hook.getUrl(), webhook.getUrl());
    }
}
