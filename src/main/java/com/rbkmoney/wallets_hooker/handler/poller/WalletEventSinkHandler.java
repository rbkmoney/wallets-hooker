package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.fistful.wallet.Event;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.wallets_hooker.constant.EventTopic;
import com.rbkmoney.wallets_hooker.dao.EventLogDao;
import com.rbkmoney.wallets_hooker.handler.poller.impl.wallet.AbstractWalletEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventSinkHandler implements EventHandler<SinkEvent> {

    private final List<AbstractWalletEventHandler> eventHandlers;
    private final EventLogDao eventLogDao;

    @Override
    public EventAction handle(SinkEvent sinkEvent, String subsKey) {
        try {
            handleEvents(sinkEvent, sinkEvent.getPayload());
            eventLogDao.create(sinkEvent.getId(), EventTopic.WALLET);
        } catch (RuntimeException e) {
            log.error("Error when polling wallet event with id={}", sinkEvent.getId(), e);
            return EventAction.DELAYED_RETRY;
        }
        return EventAction.CONTINUE;
    }

    private void handleEvents(SinkEvent sinkEvent, Event payload) {
        payload.getChanges().forEach(cc -> eventHandlers.forEach(ph -> {
            if (ph.accept(cc)) {
                ph.handle(cc, sinkEvent);
            }
        }));
    }

}
