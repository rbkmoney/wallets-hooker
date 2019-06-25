package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WithdrawalSucceededHandler extends AbstractWithdrawalEventHandler {

    private final WithdrawalReferenceDao withdrawalReferenceDao;
    private final WebHookDao webHookDao;
    private final WithdrawalStatusChangedHookMessageGenerator withdrawalStatusChangedHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private Filter filter;

    public WithdrawalSucceededHandler(WithdrawalReferenceDao withdrawalReferenceDao, WebHookDao webHookDao,
                                      WithdrawalStatusChangedHookMessageGenerator withdrawalStatusChangedHookMessageGenerator,
                                      WebHookMessageSenderService webHookMessageSenderService) {
        this.withdrawalReferenceDao = withdrawalReferenceDao;
        this.webHookDao = webHookDao;
        this.withdrawalStatusChangedHookMessageGenerator = withdrawalStatusChangedHookMessageGenerator;
        this.webHookMessageSenderService = webHookMessageSenderService;
        filter = new PathConditionFilter(new PathConditionRule("status_changed.succeeded", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        String withdrawalId = event.getSource();
        WithdrawalIdentityWalletReference reference = waitReferenceWithdrawal(withdrawalId);
        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(reference.getIdentityId(), null, EventType.WITHDRAWAL_SUCCEEDED);
        Long parentId = reference.getSequenceId();
        String walletId = reference.getWalletId();
        webHookModels.stream()
                .filter(webHook -> webHook.getWalletId() == null || webHook.getWalletId().equals(walletId))
                .map(webhook -> withdrawalStatusChangedHookMessageGenerator.generate(change.getStatusChanged(), webhook,
                        event.getId(), parentId))
                .forEach(webHookMessageSenderService::send);
    }

    private WithdrawalIdentityWalletReference waitReferenceWithdrawal(String withdrawalId) {
        WithdrawalIdentityWalletReference reference = withdrawalReferenceDao.get(withdrawalId);
        while (reference == null) {
            log.warn("Waiting withdrawal create: {} !", withdrawalId);
            try {
                Thread.sleep(500L);
                reference = withdrawalReferenceDao.get(withdrawalId);
            } catch (InterruptedException e) {
                log.error("Error when waiting withdrawal create: {} e: ", withdrawalId, e);
                Thread.currentThread().interrupt();
            }

        }
        return reference;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
