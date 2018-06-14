package com.rbkmoney.wallets_hooker.handler.poller;

import com.rbkmoney.damsel.event_stock.StockEvent;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.eventstock.client.EventAction;
import com.rbkmoney.eventstock.client.EventHandler;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventStockHandler implements EventHandler<StockEvent> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    List<Handler> pollingEventHandlers;
    private static final int INITIAL_VALUE = 3;
    private final AtomicInteger count = new AtomicInteger(INITIAL_VALUE);

    public EventStockHandler(List<Handler> pollingEventHandlers) {
        this.pollingEventHandlers = pollingEventHandlers;
    }

    @Override
    public EventAction handle(StockEvent stockEvent, String subsKey) {
        EventPayload payload = stockEvent.getSourceEvent().getProcessingEvent().getPayload();
        List changes;
        if (payload.isSetInvoiceChanges()) {
            changes = payload.getInvoiceChanges();
        } else if (payload.isSetCustomerChanges()) {
            changes = payload.getCustomerChanges();
        } else return EventAction.CONTINUE;

        long id = stockEvent.getSourceEvent().getProcessingEvent().getId();

        for (Object cc : changes) {
            for (Handler pollingEventHandler : pollingEventHandlers) {
                if (pollingEventHandler.accept(cc)) {
                    try {
                        log.info("We got an event {}", new TBaseProcessor().process(stockEvent, JsonHandler.newPrettyJsonInstance()));
                        pollingEventHandler.handle(cc, stockEvent);
                    } catch (DaoException e) {
                        log.error("DaoException when poller handling with eventId {}", id, e);
                        if (count.decrementAndGet() > 0) {
                            log.warn("Retry handle with eventId {}", id);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e1) {
                                log.warn("Waiting for retry is interrupted");
                            }
                            return EventAction.RETRY;
                        }
                    } catch (Exception e) {
                        log.error("Error when poller handling with id {}",  id, e);
                    }
                    break;
                }
            }
        }
        count.set(INITIAL_VALUE);
        return EventAction.CONTINUE;
    }
}
