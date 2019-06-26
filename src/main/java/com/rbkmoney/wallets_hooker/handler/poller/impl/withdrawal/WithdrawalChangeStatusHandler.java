package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator.WithdrawalStatusChangedHookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalChangeStatusHandler {

    private final WithdrawalReferenceDao withdrawalReferenceDao;
    private final WebHookDao webHookDao;
    private final WithdrawalStatusChangedHookMessageGenerator withdrawalStatusChangedHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;

    public void handleChangeStatus(Change change, SinkEvent event, String withdrawalId, EventType eventType) {
        WithdrawalIdentityWalletReference reference = waitReferenceWithdrawal(withdrawalId);
        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(reference.getIdentityId(), null, eventType);
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

}
