package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.dao.wallet.WalletReferenceDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator.WithdrawalCreatedHookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalCreatedHandler extends AbstractWithdrawalEventHandler {

    private final WithdrawalReferenceDao withdrawalReferenceDao;
    private final DestinationReferenceDao destinationReferenceDao;
    private final WalletReferenceDao walletReferenceDao;
    private final WebHookDao webHookDao;
    private final WithdrawalCreatedHookMessageGenerator withdrawalCreatedHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private Filter filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));

    @Override
    public void handle(Change change, SinkEvent event) {
        Withdrawal withdrawal = change.getCreated();
        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(withdrawal.getDestination());
        WalletIdentityReference walletIdentityReference = walletReferenceDao.get(event.getSource());
        while (destinationIdentityReference == null || walletIdentityReference == null) {
            log.warn("Waiting destination: {} or wallet: {} !", withdrawal.getDestination(), event.getSource());
            try {
                Thread.sleep(500L);
                destinationIdentityReference = destinationReferenceDao.get(withdrawal.getDestination());
                walletIdentityReference = walletReferenceDao.get(event.getSource());
            } catch (InterruptedException e) {
                log.error("Error when waiting destination: {} or wallet: {} e: ", withdrawal.getDestination(), event.getSource(), e);
                Thread.currentThread().interrupt();
            }
        }
        createReference(withdrawal, destinationIdentityReference, event.getPayload().sequence);

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(destinationIdentityReference.getIdentityId(), null, EventType.WITHDRAWAL_CREATED);
        if (!destinationIdentityReference.getIdentityId().equals(walletIdentityReference.getIdentityId())) {
            List<WebHookModel> webHookModelsWallets = webHookDao.getModelByIdentityAndWalletId(walletIdentityReference.getIdentityId(), null, EventType.WITHDRAWAL_CREATED);
            webHookModels.addAll(webHookModelsWallets);
        }

        webHookModels.stream()
                .filter(webHook -> webHook.getWalletId() == null || webHook.getWalletId().equals(event.getSource()))
                .map(webhook -> withdrawalCreatedHookMessageGenerator.generate(withdrawal, webhook, event.getId(), 0L))
                .forEach(webHookMessageSenderService::send);
    }

    private void createReference(Withdrawal withdrawal, DestinationIdentityReference destinationIdentityReference, int sequenceId) {
        WithdrawalIdentityWalletReference reference = new WithdrawalIdentityWalletReference();
        reference.setIdentityId(destinationIdentityReference.getIdentityId());
        reference.setWalletId(withdrawal.getSource());
        reference.setWithdrawalId(withdrawal.getId());
        reference.setEventId(withdrawal.getId());
        reference.setSequenceId((long) sequenceId);
        withdrawalReferenceDao.create(reference);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
