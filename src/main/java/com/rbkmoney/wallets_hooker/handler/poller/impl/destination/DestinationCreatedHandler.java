package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorService;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DestinationCreatedHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final WebHookMessageGeneratorService webHookMessageGeneratorService;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private final WebHookDao webHookDao;

    private Filter filter;

    public DestinationCreatedHandler(DestinationReferenceDao destinationReferenceDao, WebHookDao webHookDao,
                                     WebHookMessageGeneratorService webHookMessageGeneratorService, WebHookMessageSenderService webHookMessageSenderService) {
        this.destinationReferenceDao = destinationReferenceDao;
        this.webHookDao = webHookDao;
        this.webHookMessageGeneratorService = webHookMessageGeneratorService;
        this.webHookMessageSenderService = webHookMessageSenderService;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(com.rbkmoney.fistful.destination.Change change, com.rbkmoney.fistful.destination.SinkEvent sinkEvent) {
        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(sinkEvent.getSource());
        Destination created = change.getCreated();

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(destinationIdentityReference.getIdentityId(),
                null, EventType.WITHDRAWAL_CREATED);

        webHookModels.stream()
                .map(webhook -> webHookMessageGeneratorService.generate(created, webhook))
                .forEach(webHookMessageSenderService::send);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
