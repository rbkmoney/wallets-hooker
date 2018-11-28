package com.rbkmoney.wallets_hooker.handler.poller.impl.wallet;

import com.rbkmoney.fistful.wallet.Event;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.github.benas.randombeans.api.EnhancedRandom.random;

public class WalletCreatedHandlerTest extends AbstractIntegrationTest {

    @Autowired
    private WalletCreatedHandler walletCreatedHandler;

    @Test
    public void handle() {
        SinkEvent sinkEvent = new SinkEvent();
        Event payload = new Event();
        sinkEvent.setId(random(Long.class));
        payload.setOccuredAt("2016-03-22T06:12:27Z");
        sinkEvent.setSource(random(String.class));
        sinkEvent.setPayload(payload);
        walletCreatedHandler.handle(null, sinkEvent);
    }
}