package com.rbkmoney.wallets_hooker.handler.destination;

import com.rbkmoney.fistful.destination.StatusChange;
import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.handler.destination.generator.DestinationStatusChangeHookMessageGenerator;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.MessageGenParams;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationUnauthorizedHandler implements DestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationStatusChangeHookMessageGenerator destinationStatusChangeHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;
    private final WebHookDao webHookDao;

    @SuppressWarnings("rawtypes")
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "status.changed.unauthorized",
            new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange change, MachineEvent event) {
        String destinationId = event.getSourceId();
        log.info("Start handling DestinationUnauthorizedChange: destinationId={}", destinationId);

        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(event.getSourceId());

        webHookDao.getByIdentityAndEventType(destinationIdentityReference.getIdentityId(), EventType.DESTINATION_UNAUTHORIZED)
                .stream()
                .map(webhook -> generateDestinationStatusChangeHookMsg(
                        change.getChange().getStatus(),
                        webhook,
                        event.getSourceId(),
                        event.getEventId(),
                        Long.valueOf(destinationIdentityReference.getEventId()),
                        event.getCreatedAt(),
                        destinationIdentityReference.getExternalId()))
                .forEach(webHookMessageSenderService::send);

        log.info("Finish handling DestinationUnauthorizedChange: destinationId={}", destinationId);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Filter getFilter() {
        return filter;
    }

    private WebhookMessage generateDestinationStatusChangeHookMsg(
            StatusChange statusChange, WebHookModel webhook,
            String sourceId,
            long eventId,
            Long parentId,
            String createdAt,
            String externalId) {
        MessageGenParams messageGenParams = MessageGenParams.builder()
                .sourceId(sourceId)
                .eventId(eventId)
                .parentId(parentId)
                .createdAt(createdAt)
                .externalId(externalId)
                .build();

        return destinationStatusChangeHookMessageGenerator.generate(statusChange, webhook, messageGenParams);
    }
}
