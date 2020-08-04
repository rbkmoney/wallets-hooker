package com.rbkmoney.wallets_hooker.service.kafka;

import com.rbkmoney.fistful.wallet.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.wallets_hooker.dao.EventLogDao;
import com.rbkmoney.wallets_hooker.domain.enums.EventTopic;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.EventLog;
import com.rbkmoney.wallets_hooker.handler.wallet.WalletEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletEventService {

    private final List<WalletEventHandler> walletEventHandlers;
    private final MachineEventParser<TimestampedChange> parser;
    private final EventLogDao eventLogDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleEvents(List<MachineEvent> machineEvents) {
        machineEvents.forEach(this::handleIfAccept);
    }

    private void handleIfAccept(MachineEvent machineEvent) {
        Optional<EventLog> duplicate = eventLogDao.get(
                machineEvent.getSourceId(),
                machineEvent.getEventId(),
                EventTopic.wallet);

        if (duplicate.isPresent()) {
            return;
        }

        TimestampedChange change = parser.parse(machineEvent);

        if (change.isSetChange()) {
            walletEventHandlers.stream()
                    .filter(handler -> handler.accept(change))
                    .forEach(handler -> handler.handle(change, machineEvent));
        }

        eventLogDao.create(
                machineEvent.getSourceId(),
                machineEvent.getEventId(),
                EventTopic.wallet);
    }
}
