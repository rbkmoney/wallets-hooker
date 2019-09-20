package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.rbkmoney.fistful.destination.AccountChange;
import com.rbkmoney.fistful.destination.Change;
import com.rbkmoney.fistful.destination.SinkEvent;
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
import com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator.DestinationCreatedHookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rbkmoney.wallets_hooker.utils.LogUtils.getLogWebHookModel;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationAccountChangeHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;
    private final DestinationMessageDaoImpl destinationMessageDao;
    private final DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator;
    private final WebHookDao webHookDao;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private Filter filter = new PathConditionFilter(new PathConditionRule("account", new IsNullCondition().not()));

    @Override
    public void handle(Change change, SinkEvent sinkEvent) {
        String destinationId = sinkEvent.getSource();
        AccountChange account = change.getAccount();
        String identityId = account.getCreated().getIdentity();

        log.info("Start handling destination event account change, destinationId={}, identityId={}", destinationId, identityId);

        createDestinationReference(sinkEvent, identityId);

        log.info("Trying to get webHookModels, destinationId={}, identityId={}", destinationId, identityId);

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(identityId, null, EventType.DESTINATION_CREATED);

        if (!webHookModels.isEmpty()) {
            log.info("webHookModels has been got, models={}", getLogWebHookModel(webHookModels));
            log.info("Trying to get destinationMessage, destinationId={}", destinationId);

            DestinationMessage destinationMessage = destinationMessageDao.get(destinationId);

            log.info("destinationMessage has been got, destinationId={}", destinationId);

            webHookModels.stream()
                    .map(webhook -> destinationCreatedHookMessageGenerator.generate(destinationMessage, webhook, destinationId, sinkEvent.getId(), sinkEvent.getCreatedAt()))
                    .forEach(webHookMessageSenderService::send);
        } else {
            log.info("webHookModels is empty, destinationId={}", destinationId);
        }

        log.info("Finish handling destination event account change, destinationId={}, identityId={}", destinationId, identityId);
    }

    private void createDestinationReference(SinkEvent sinkEvent, String identityId) {
        log.info("Trying to create destinationIdentityReference, identityId={}", identityId);

        DestinationIdentityReference destinationIdentityReference = new DestinationIdentityReference();
        destinationIdentityReference.setDestinationId(sinkEvent.getSource());
        destinationIdentityReference.setIdentityId(identityId);
        destinationIdentityReference.setEventId(String.valueOf(sinkEvent.getId()));
        destinationIdentityReference.setSequenceId((long) sinkEvent.getPayload().getSequence());

        destinationReferenceDao.create(destinationIdentityReference);

        log.info("destinationIdentityReference has been created: {} ", destinationIdentityReference.toString());
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
