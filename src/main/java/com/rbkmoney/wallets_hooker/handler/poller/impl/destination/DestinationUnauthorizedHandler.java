package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.rbkmoney.fistful.destination.Change;
import com.rbkmoney.fistful.destination.SinkEvent;
import com.rbkmoney.fistful.destination.StatusChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator.DestinationStatusChangeHookMessageGenerator;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.MessageGenParams;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationUnauthorizedHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationStatusChangeHookMessageGenerator destinationStatusChangeHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;
    private final WebHookDao webHookDao;

    private Filter filter = new PathConditionFilter(new PathConditionRule("status.changed.unauthorized", new IsNullCondition().not()));

    @Override
    public void handle(Change change, SinkEvent sinkEvent) {
        String destinationId = sinkEvent.getSource();

        log.info("Start handling destination event status unauthorized change, destinationId={}", destinationId);

        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(sinkEvent.getSource());

        List<WebHookModel> webHookModels = webHookDao.getByIdentityAndEventType(destinationIdentityReference.getIdentityId(), EventType.DESTINATION_UNAUTHORIZED);

        webHookModels.stream()
                .map(webhook -> generateDestinationStatusChangeHookMsg(change.getStatus(), webhook, sinkEvent.getSource(),
                            sinkEvent.getId(), Long.valueOf(destinationIdentityReference.getEventId()),
                            sinkEvent.getCreatedAt(), destinationIdentityReference.getExternalId())
                )
                .forEach(webHookMessageSenderService::send);

        log.info("Finish handling destination event status unauthorized change, destinationId={}", destinationId);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private WebhookMessage generateDestinationStatusChangeHookMsg(StatusChange statusChange, WebHookModel webhook,
                                                                  String sourceId, long eventId, Long parentId,
                                                                  String createdAt, String externalId) {
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
