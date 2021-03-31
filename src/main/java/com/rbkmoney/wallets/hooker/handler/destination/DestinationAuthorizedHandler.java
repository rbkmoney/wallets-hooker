package com.rbkmoney.wallets.hooker.handler.destination;

import com.rbkmoney.fistful.destination.StatusChange;
import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets.hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets.hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.domain.enums.EventType;
import com.rbkmoney.wallets.hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets.hooker.handler.destination.generator.DestinationStatusChangeHookMessageGenerator;
import com.rbkmoney.wallets.hooker.model.MessageGenParams;
import com.rbkmoney.wallets.hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationAuthorizedHandler implements DestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationStatusChangeHookMessageGenerator destinationStatusChangeHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;
    private final WebHookDao webHookDao;

    @Override
    public boolean accept(TimestampedChange change) {
        return change.getChange().isSetStatus()
                && change.getChange().getStatus().isSetChanged()
                && change.getChange().getStatus().getChanged().isSetAuthorized();
    }

    @Override
    public void handle(TimestampedChange change, MachineEvent event) {
        String destinationId = event.getSourceId();
        log.info("Start handling DestinationAuthorizedChange: destinationId={}", destinationId);

        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(destinationId);

        webHookDao.getByIdentityAndEventType(destinationIdentityReference.getIdentityId(),
                EventType.DESTINATION_AUTHORIZED)
                .stream()
                .map(webhook -> generateDestinationChangeHookMsg(
                        change.getChange().getStatus(),
                        webhook,
                        event.getSourceId(),
                        event.getEventId(),
                        Long.valueOf(destinationIdentityReference.getEventId()),
                        event.getCreatedAt(),
                        destinationIdentityReference.getExternalId()))
                .forEach(webHookMessageSenderService::send);

        log.info("Finish handling DestinationAuthorizedChange: destinationId={}", destinationId);
    }

    private WebhookMessage generateDestinationChangeHookMsg(
            StatusChange status,
            WebHookModel webhook,
            String sourcedId,
            long eventId,
            Long parentId,
            String createdAt,
            String externalId) {
        MessageGenParams messageGenParams = MessageGenParams.builder()
                .sourceId(sourcedId)
                .eventId(eventId)
                .parentId(parentId)
                .createdAt(createdAt)
                .externalId(externalId)
                .build();

        return destinationStatusChangeHookMessageGenerator.generate(status, webhook, messageGenParams);
    }
}
