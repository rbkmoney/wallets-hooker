package com.rbkmoney.wallets_hooker.handler.destination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.wallets_hooker.converter.DestinationToDestinationMessageConverter;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationMessageDaoImpl;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.exception.HandleEventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationCreatedHandler implements DestinationEventHandler {

    private final DestinationMessageDaoImpl destinationMessageDao;
    private final DestinationToDestinationMessageConverter destinationToDestinationMessageConverter;
    private final ObjectMapper objectMapper;

    @Override
    public boolean accept(TimestampedChange change) {
        return change.getChange().isSetCreated();
    }

    @Override
    public void handle(TimestampedChange change, MachineEvent event) {
        try {
            String destinationId = event.getSourceId();
            log.info("Start handling DestinationCreatedChange: destinationId={}", destinationId);

            Destination destination = destinationToDestinationMessageConverter.convert(change.getChange().getCreated());
            destination.setId(destinationId);

            DestinationMessage destinationMessage = new DestinationMessage();
            destinationMessage.setDestinationId(destinationId);
            destinationMessage.setMessage(objectMapper.writeValueAsString(destination));

            destinationMessageDao.create(destinationMessage);

            log.info("Finish handling DestinationCreatedChange: destinationId={}", destinationId);
        } catch (JsonProcessingException e) {
            log.error("Error while handling DestinationCreatedChange: {}", change, e);
            throw new HandleEventException("Error while handling DestinationCreatedChange", e);
        }
    }

}
