package com.rbkmoney.wallets_hooker.handler.destination;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.AccountChange;
import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationMessageDaoImpl;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.exception.HandleEventException;
import com.rbkmoney.wallets_hooker.handler.destination.generator.DestinationCreatedHookMessageGenerator;
import com.rbkmoney.wallets_hooker.model.MessageGenParams;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationAccountChangeHandler implements DestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationMessageDaoImpl destinationMessageDao;
    private final DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator;
    private final WebHookDao webHookDao;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private final ObjectMapper objectMapper;

    @SuppressWarnings("rawtypes")
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "account",
            new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange change, MachineEvent event) {
        try {
            String destinationId = event.getSourceId();
            AccountChange account = change.getChange().getAccount();
            String identityId = account.getCreated().getIdentity();

            log.info("Start handling DestinationAccountChange: destinationId={}, identityId={}", destinationId, identityId);

            DestinationMessage destinationMessage = destinationMessageDao.get(destinationId);
            Destination destination = objectMapper.readValue(destinationMessage.getMessage(), Destination.class);
            createDestinationReference(event, identityId, destination.getExternalID());

            webHookDao.getByIdentityAndEventType(identityId, EventType.DESTINATION_CREATED)
                    .stream()
                    .map(webhook -> generateDestinationCreateHookMsg(
                            destinationMessage,
                            webhook,
                            destinationId,
                            event.getEventId(),
                            event.getCreatedAt()))
                    .forEach(webHookMessageSenderService::send);

            log.info("Finish handling destination event account change: destinationId={}, identityId={}", destinationId, identityId);

        } catch (IOException e) {
            log.error("Error while handling DestinationAccountChange: {}", change, e);
            throw new HandleEventException("Error while handling DestinationAccountChange", e);
        }
    }

    private WebhookMessage generateDestinationCreateHookMsg(
            DestinationMessage destinationMessage,
            WebHookModel webhook,
            String sourceId,
            Long eventId,
            String createdAt) {
        MessageGenParams messageGenParams = MessageGenParams.builder()
                .sourceId(sourceId)
                .eventId(eventId)
                .createdAt(createdAt)
                .build();
        return destinationCreatedHookMessageGenerator.generate(destinationMessage, webhook, messageGenParams);
    }

    private void createDestinationReference(MachineEvent event, String identityId, String externalID) {
        DestinationIdentityReference destinationIdentityReference = new DestinationIdentityReference();
        destinationIdentityReference.setDestinationId(event.getSourceId());
        destinationIdentityReference.setIdentityId(identityId);
        destinationIdentityReference.setEventId(String.valueOf(event.getEventId()));
        destinationIdentityReference.setExternalId(externalID);

        destinationReferenceDao.create(destinationIdentityReference);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Filter getFilter() {
        return filter;
    }
}
