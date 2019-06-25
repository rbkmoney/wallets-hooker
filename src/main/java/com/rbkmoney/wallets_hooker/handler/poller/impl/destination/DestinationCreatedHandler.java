package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.wallets_hooker.converter.DestinationToDestinationMessageConverter;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationMessageDaoImpl;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DestinationCreatedHandler extends AbstractDestinationEventHandler {

    private final DestinationMessageDaoImpl destinationMessageDao;
    private final DestinationToDestinationMessageConverter destinationToDestinationMessageConverter;
    private final ObjectMapper objectMapper;

    private Filter filter;

    public DestinationCreatedHandler(DestinationMessageDaoImpl destinationMessageDao, ObjectMapper objectMapper,
                                     DestinationToDestinationMessageConverter destinationToDestinationMessageConverter
    ) {
        this.destinationMessageDao = destinationMessageDao;
        this.objectMapper = objectMapper;
        this.destinationToDestinationMessageConverter = destinationToDestinationMessageConverter;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(com.rbkmoney.fistful.destination.Change change, com.rbkmoney.fistful.destination.SinkEvent sinkEvent) {
        try {
            Destination destination = destinationToDestinationMessageConverter.convert(change.getCreated());
            DestinationMessage destinationMessage = new DestinationMessage();
            destinationMessage.setDestinationId(change.getCreated().getId());
            destinationMessage.setMessage(objectMapper.writeValueAsString(destination));
            destinationMessageDao.create(destinationMessage);
        } catch (JsonProcessingException e) {
            log.error("Error when handle DestinationCreated change: {} e: ", change, e);
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
