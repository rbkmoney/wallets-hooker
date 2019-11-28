package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.AccountChange;
import com.rbkmoney.fistful.destination.Change;
import com.rbkmoney.fistful.destination.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationMessageDaoImpl;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.exception.HandleEventException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator.DestinationCreatedHookMessageGenerator;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.GeneratorParam;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationAccountChangeHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationMessageDaoImpl destinationMessageDao;
    private final DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator;
    private final WebHookDao webHookDao;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private final ObjectMapper objectMapper;

    private Filter filter = new PathConditionFilter(new PathConditionRule("account", new IsNullCondition().not()));

    @Override
    public void handle(Change change, SinkEvent sinkEvent) {
        try {
            String destinationId = sinkEvent.getSource();
            AccountChange account = change.getAccount();
            String identityId = account.getCreated().getIdentity();

            log.info("Start handling destination event account change, destinationId={}, identityId={}", destinationId, identityId);

            DestinationMessage destinationMessage = destinationMessageDao.get(destinationId);

            Destination destination = objectMapper.readValue(destinationMessage.getMessage(), Destination.class);

            createDestinationReference(sinkEvent, identityId, destination.getExternalID());

            List<WebHookModel> webHookModels = webHookDao.getByIdentityAndEventType(identityId, EventType.DESTINATION_CREATED);

            webHookModels.stream()
                    .map(webhook -> {
                        GeneratorParam generatorParam = GeneratorParam.builder()
                                .sourceId(destinationId)
                                .eventId(sinkEvent.getId())
                                .createdAt(sinkEvent.getCreatedAt())
                                .build();
                        return destinationCreatedHookMessageGenerator.generate(destinationMessage, webhook, generatorParam);
                    })
                    .forEach(webHookMessageSenderService::send);

            log.info("Finish handling destination event account change, destinationId={}, identityId={}", destinationId, identityId);

        } catch (IOException e) {
            log.error("Error when handle DestinationCreated change: {} e: ", change, e);
            throw new HandleEventException("Error when handle DestinationCreated change", e);
        }
    }

    private void createDestinationReference(SinkEvent sinkEvent, String identityId, String externalID) {
        DestinationIdentityReference destinationIdentityReference = new DestinationIdentityReference();
        destinationIdentityReference.setDestinationId(sinkEvent.getSource());
        destinationIdentityReference.setIdentityId(identityId);
        destinationIdentityReference.setEventId(String.valueOf(sinkEvent.getId()));
        destinationIdentityReference.setSequenceId((long) sinkEvent.getPayload().getSequence());
        destinationIdentityReference.setExternalId(externalID);

        destinationReferenceDao.create(destinationIdentityReference);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
