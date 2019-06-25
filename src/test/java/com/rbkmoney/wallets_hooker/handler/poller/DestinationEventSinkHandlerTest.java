package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.fistful.destination.Change;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.Event;
import com.rbkmoney.fistful.destination.SinkEvent;
import com.rbkmoney.wallets_hooker.HookerApplication;
import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HookerApplication.class)
public class DestinationEventSinkHandlerTest extends AbstractPostgresIntegrationTest {

    @Autowired
    DestinationEventSinkHandler destinationEventSinkHandler;

    @MockBean
    WebHookMessageSenderService webHookMessageSenderService;

    @Test
    public void handle() {
        SinkEvent sinkEvent = new SinkEvent();
        Event payload = new Event();
        ArrayList<Change> changes = new ArrayList<>();
        Change change = new Change();
        Destination destination = new Destination();
        destination.setId("destId");
        change.setCreated(destination);
        changes.add(change);
        payload.setChanges(changes);
        payload.setSequence(1);
        sinkEvent.setPayload(payload);
        EventAction test = destinationEventSinkHandler.handle(sinkEvent, "test");

        Assert.assertEquals(test, EventAction.CONTINUE);
    }

}