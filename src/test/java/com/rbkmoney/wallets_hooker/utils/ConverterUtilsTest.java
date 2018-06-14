package com.rbkmoney.wallets_hooker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.wallets_hooker.dao.WebhookAdditionalFilter;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static com.rbkmoney.wallets_hooker.utils.ConverterUtils.*;

public class ConverterUtilsTest {

    @Test
    public void testBuildJsonAsString() throws JsonProcessingException {
        WalletsMessage message = ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_CREATED", "partyId", "walletId", "2016-03-22T06:12:27Z", 123);
        System.out.println(getObjectMapper().writeValueAsString(message.getEvent()));
    }

    @Test
    public void testConvertEventFilter() {
        Collection<WebhookAdditionalFilter> eventTypeCodeSet = new HashSet<>();
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.WALLET_WITHDRAWAL_CREATED));
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.WALLET_WITHDRAWAL_SUCCEEDED));
        eventTypeCodeSet.add(new WebhookAdditionalFilter(EventType.WALLET_WITHDRAWAL_FAILED));
        Assert.assertEquals(convertWebhookAdditionalFilter(convertEventFilter(eventTypeCodeSet)).size(), 3);
    }
}
