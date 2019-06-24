package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorService;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WithdrawalCreatedHandler extends AbstractWithdrawalEventHandler {

    private final WithdrawalReferenceDao withdrawalReferenceDao;
    private final DestinationReferenceDao destinationReferenceDao;
    private final WebHookDao webHookDao;
    private final WebHookMessageGeneratorService webHookMessageGeneratorService;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private Filter filter;

    public WithdrawalCreatedHandler(WithdrawalReferenceDao withdrawalReferenceDao, DestinationReferenceDao destinationReferenceDao,
                                    WebHookDao webHookDao, WebHookMessageGeneratorService webHookMessageGeneratorService,
                                    WebHookMessageSenderService webHookMessageSenderService) {
        this.withdrawalReferenceDao = withdrawalReferenceDao;
        this.destinationReferenceDao = destinationReferenceDao;
        this.webHookDao = webHookDao;
        this.webHookMessageGeneratorService = webHookMessageGeneratorService;
        this.webHookMessageSenderService = webHookMessageSenderService;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        Withdrawal withdrawal = change.getCreated();
        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(withdrawal.getDestination());

        createReference(withdrawal, destinationIdentityReference);

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(destinationIdentityReference.getIdentityId(), withdrawal.getSource(), EventType.WITHDRAWAL_CREATED);

        webHookModels.stream()
                .map(webhook -> webHookMessageGeneratorService.generate(withdrawal, webhook))
                .forEach(webHookMessageSenderService::send);


    }

    private void createReference(Withdrawal withdrawal, DestinationIdentityReference destinationIdentityReference) {
        WithdrawalIdentityWalletReference reference = new WithdrawalIdentityWalletReference();
        reference.setIdentityId(destinationIdentityReference.getIdentityId());
        reference.setWalletId(withdrawal.getSource());
        reference.setWithdrawalId(withdrawal.getId());
        withdrawalReferenceDao.create(reference);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
