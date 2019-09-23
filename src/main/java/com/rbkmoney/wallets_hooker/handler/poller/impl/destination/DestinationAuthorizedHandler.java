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
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationAuthorizedHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationStatusChangeHookMessageGenerator destinationStatusChangeHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;
    private final WebHookDao webHookDao;

    private Filter filter = new PathConditionFilter(new PathConditionRule("status.changed.authorized", new IsNullCondition().not()));

    @Override
    public void handle(Change change, SinkEvent sinkEvent) {
        String destinationId = sinkEvent.getSource();

        log.info("Start handling destination event status authorized change, destinationId={}", destinationId);

        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(destinationId);

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(destinationIdentityReference.getIdentityId(), null, EventType.DESTINATION_AUTHORIZED);

        if (!webHookModels.isEmpty()) {
            StatusChange status = change.getStatus();

            webHookModels.stream()
                    .map(webhook -> destinationStatusChangeHookMessageGenerator.generate(status, webhook, sinkEvent.getSource(),
                            sinkEvent.getId(), Long.valueOf(destinationIdentityReference.getEventId()), sinkEvent.getCreatedAt()))
                    .forEach(webHookMessageSenderService::send);
        }

        log.info("Finish handling destination event status authorized change, destinationId={}", destinationId);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
