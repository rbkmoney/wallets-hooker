package com.rbkmoney.wallets_hooker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.wallets_hooker.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static com.rbkmoney.wallets_hooker.utils.ConverterUtils.*;

public class ConverterUtilsTest {

    @Test
    public void testBuildJsonAsString() throws JsonProcessingException {
        WithdrawalMessage message = BuildUtils.buildWithdrawalMessage(EventType.WITHDRAWAL_CREATED, "partyId", "withdrawalId");
    }

    @Test
    public void testConvertEventFilter() {
        Collection<Hook.WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        eventTypeCodeSet.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL));
        eventTypeCodeSet.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL));
        eventTypeCodeSet.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL));
        Assert.assertEquals(convertWebhookAdditionalFilter(convertEventFilter(eventTypeCodeSet)).size(), 3);
    }
}
