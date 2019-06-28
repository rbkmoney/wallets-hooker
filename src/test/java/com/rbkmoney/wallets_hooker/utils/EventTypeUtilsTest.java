package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.fistful.webhooker.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class EventTypeUtilsTest {

    @Test
    public void convertEventTypes() {

        WebhookParams event = new WebhookParams();
        EventFilter eventFilter = new EventFilter();
        LinkedHashSet<EventType> types = new LinkedHashSet<>();
        DestinationEventType value = new DestinationEventType();
        value.setCreated(new DestinationCreated());
        types.add(EventType.destination(value));
        eventFilter.setTypes(types);
        event.setEventFilter(eventFilter);
        Set<com.rbkmoney.wallets_hooker.domain.enums.EventType> eventTypes = EventTypeUtils.convertEventTypes(event);

        Assert.assertEquals(1, eventTypes.size());
        Assert.assertTrue(eventTypes.contains(com.rbkmoney.wallets_hooker.domain.enums.EventType.DESTINATION_CREATED));
    }
}