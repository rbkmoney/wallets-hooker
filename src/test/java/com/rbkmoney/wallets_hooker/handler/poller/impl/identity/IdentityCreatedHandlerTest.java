package com.rbkmoney.wallets_hooker.handler.poller.impl.identity;

import com.rbkmoney.fistful.identity.Change;
import com.rbkmoney.fistful.identity.Event;
import com.rbkmoney.fistful.identity.Identity;
import com.rbkmoney.fistful.identity.SinkEvent;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static io.github.benas.randombeans.api.EnhancedRandom.random;

public class IdentityCreatedHandlerTest extends AbstractIntegrationTest {

    @Autowired
    private IdentityCreatedHandler identityCreatedHandler;

    @Test
    public void handle() {
        Change change = new Change();
        Identity identity = new Identity();
        identity.setParty(random(String.class));
        change.setCreated(identity);
        SinkEvent event = new SinkEvent();
        Event payload = new Event();
        event.setId(random(Long.class));
        payload.setOccuredAt("2016-03-22T06:12:27Z");
        event.setPayload(payload);
        event.setSource(random(String.class));
        identityCreatedHandler.handle(change, event);
    }
}