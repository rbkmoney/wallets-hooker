package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

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

    private Filter filter = new PathConditionFilter(new PathConditionRule("status_changed.succeeded", new IsNullCondition().not()));

    @Override
    public void handle(com.rbkmoney.fistful.destination.Change change, com.rbkmoney.fistful.destination.SinkEvent sinkEvent) {
        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(sinkEvent.getSource());

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(destinationIdentityReference.getIdentityId(),
                null, EventType.DESTINATION_AUTHORIZED);

        StatusChange status = change.getStatus();
        webHookModels.stream()
                .map(webhook -> destinationStatusChangeHookMessageGenerator.generate(status, webhook, sinkEvent.getId(), 0L))
                .forEach(webHookMessageSenderService::send);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
