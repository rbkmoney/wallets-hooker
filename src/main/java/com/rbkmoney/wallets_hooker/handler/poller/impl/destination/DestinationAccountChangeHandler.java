package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.rbkmoney.fistful.destination.AccountChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationMessageDaoImpl;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DestinationAccountChangeHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationMessageDaoImpl destinationMessageDao;
    private final DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator;
    private final WebHookDao webHookDao;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private Filter filter;

    public DestinationAccountChangeHandler(DestinationReferenceDao destinationReferenceDao, DestinationMessageDaoImpl destinationMessageDao, DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator, WebHookDao webHookDao, WebHookMessageSenderService webHookMessageSenderService) {
        this.destinationReferenceDao = destinationReferenceDao;
        this.destinationMessageDao = destinationMessageDao;
        this.destinationCreatedHookMessageGenerator = destinationCreatedHookMessageGenerator;
        this.webHookDao = webHookDao;
        this.webHookMessageSenderService = webHookMessageSenderService;
        filter = new PathConditionFilter(new PathConditionRule("account", new IsNullCondition().not()));
    }

    @Override
    public void handle(com.rbkmoney.fistful.destination.Change change, com.rbkmoney.fistful.destination.SinkEvent sinkEvent) {
        AccountChange account = change.getAccount();
        DestinationIdentityReference reference = new DestinationIdentityReference();
        reference.setDestinationId(sinkEvent.getSource());
        String identity = account.getCreated().getIdentity();
        reference.setIdentityId(identity);
        reference.setEventId(sinkEvent.getSource());
        reference.setSequenceId((long) sinkEvent.getPayload().getSequence());
        destinationReferenceDao.create(reference);

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(identity,
                null, EventType.DESTINATION_CREATED);

        DestinationMessage destinationMessage = destinationMessageDao.get(sinkEvent.getSource());

        webHookModels.stream()
                .map(webhook -> destinationCreatedHookMessageGenerator.generate(destinationMessage, webhook, sinkEvent.getId(), 0L))
                .forEach(webHookMessageSenderService::send);
        log.info("Handle destination event account change with account: {} ", account);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
