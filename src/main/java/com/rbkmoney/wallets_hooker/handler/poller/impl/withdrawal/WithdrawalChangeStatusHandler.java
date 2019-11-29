package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.StatusChange;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.exception.HandleEventException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.MessageGenParams;
import com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator.WithdrawalStatusChangedHookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalChangeStatusHandler {

    @Value("${waiting.reference.period}")
    private int waitingPollPeriod;

    private final WithdrawalReferenceDao withdrawalReferenceDao;
    private final WebHookDao webHookDao;
    private final WithdrawalStatusChangedHookMessageGenerator withdrawalStatusChangedHookMessageGenerator;
    private final WebHookMessageSenderService webHookMessageSenderService;

    public void handleChangeStatus(Change change, SinkEvent sinkEvent, String withdrawalId, EventType eventType) {
        try {
            WithdrawalIdentityWalletReference reference = waitReferenceWithdrawal(withdrawalId);

            Long parentId = Long.valueOf(reference.getEventId());

            List<WebHookModel> webHookModels = webHookDao.getByIdentityAndEventType(reference.getIdentityId(), eventType);

            webHookModels.stream()
                    .filter(webHook -> webHook.getWalletId() == null || webHook.getWalletId().equals(reference.getWalletId()))
                    .map(webhook -> {
                        return generateWithdrawalStatusChangeHookMsg(change.getStatusChanged(), webhook, withdrawalId, sinkEvent.getId(),
                                parentId, sinkEvent.getCreatedAt(), reference.getExternalId());
                    })
                    .forEach(webHookMessageSenderService::send);
        } catch (Exception e) {
            log.error("WithdrawalChangeStatusHandler error when handle change: {}, withdrawalId: {} e: ", change, withdrawalId, e);
            throw new HandleEventException("WithdrawalChangeStatusHandler error when handle change!", e);
        }
    }

    private WithdrawalIdentityWalletReference waitReferenceWithdrawal(String withdrawalId) {
        WithdrawalIdentityWalletReference withdrawalIdentityWalletReference = withdrawalReferenceDao.get(withdrawalId);
        while (withdrawalIdentityWalletReference == null) {
            log.info("Waiting withdrawal create: {} !", withdrawalId);
            try {
                Thread.sleep(waitingPollPeriod);
                withdrawalIdentityWalletReference = withdrawalReferenceDao.get(withdrawalId);
            } catch (InterruptedException e) {
                log.error("Error when waiting withdrawal create: {} e: ", withdrawalId, e);
                Thread.currentThread().interrupt();
            }
        }

        return withdrawalIdentityWalletReference;
    }

    private WebhookMessage generateWithdrawalStatusChangeHookMsg(StatusChange statusChanged, WebHookModel webhook,
                                                                 String withdrawalId, long eventId, Long parentId,
                                                                 String createdAt, String externalId) {
        MessageGenParams messageGenParams = MessageGenParams.builder()
                .sourceId(withdrawalId)
                .eventId(eventId)
                .parentId(parentId)
                .createdAt(createdAt)
                .externalId(externalId)
                .build();
        return withdrawalStatusChangedHookMessageGenerator.generate(statusChanged, webhook, messageGenParams);
    }

}
