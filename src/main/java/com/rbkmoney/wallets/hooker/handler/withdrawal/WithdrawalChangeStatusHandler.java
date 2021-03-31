package com.rbkmoney.wallets.hooker.handler.withdrawal;

import com.rbkmoney.fistful.withdrawal.StatusChange;
import com.rbkmoney.fistful.withdrawal.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets.hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets.hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.domain.enums.EventType;
import com.rbkmoney.wallets.hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets.hooker.exception.HandleEventException;
import com.rbkmoney.wallets.hooker.handler.withdrawal.generator.WithdrawalStatusChangedHookMessageGenerator;
import com.rbkmoney.wallets.hooker.model.MessageGenParams;
import com.rbkmoney.wallets.hooker.service.WebHookMessageSenderService;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public void handleChangeStatus(
            TimestampedChange change,
            MachineEvent event,
            String withdrawalId,
            EventType eventType) {
        try {
            WithdrawalIdentityWalletReference reference = waitReferenceWithdrawal(withdrawalId);
            Long parentId = Long.valueOf(reference.getEventId());

            webHookDao.getByIdentityAndEventType(reference.getIdentityId(), eventType).stream()
                    .filter(webHook -> webHook.getWalletId() == null
                            || webHook.getWalletId().equals(reference.getWalletId()))
                    .map(webhook -> generateWithdrawalStatusChangeHookMsg(
                            change.getChange().getStatusChanged(),
                            webhook,
                            withdrawalId,
                            event.getEventId(),
                            parentId,
                            event.getCreatedAt(),
                            reference.getExternalId()))
                    .forEach(webHookMessageSenderService::send);
        } catch (Exception e) {
            log.error("Error while handling WithdrawalStatusChangedChange: {}, withdrawalId: {}", change, withdrawalId,
                    e);
            throw new HandleEventException("Error while handling WithdrawalStatusChangedChange", e);
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

    private WebhookMessage generateWithdrawalStatusChangeHookMsg(
            StatusChange statusChanged,
            WebHookModel webhook,
            String withdrawalId,
            long eventId,
            Long parentId,
            String createdAt,
            String externalId) {
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
