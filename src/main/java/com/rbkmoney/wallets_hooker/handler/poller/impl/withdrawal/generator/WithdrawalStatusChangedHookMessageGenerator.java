package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.withdrawal.WithdrawalStatus;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalFailed;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalSucceeded;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalStatusChangedHookMessageGenerator implements HookMessageGenerator<WithdrawalStatus> {

    private final WebHookMessageGeneratorServiceImpl<WithdrawalStatus> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    @Value("${parent.not.exist.id}")
    private Long parentIsNotExistId;

    @Override
    public WebhookMessage generate(WithdrawalStatus event, WebHookModel model, String sourceId, Long eventId, String createdAt) {
        return generate(event, model, sourceId, eventId, parentIsNotExistId, createdAt);
    }

    @Override
    public WebhookMessage generate(WithdrawalStatus event, WebHookModel model, String sourceId, Long eventId, Long parentId, String createdAt) {
        try {
            WebhookMessage webhookMessage = generatorService.generate(event, model, sourceId, eventId, parentId, createdAt);
            webhookMessage.setParentEventId(initPatenId(model, parentId));
            String message = initRequestBody(event, sourceId);
            webhookMessage.setRequestBody(message.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, message));
            log.info("Webhook message generated webhookMessage: {} for model: {}", webhookMessage, model);
            return webhookMessage;
        } catch (Exception e) {
            log.error("Error when generate webhookMessage e: ", e);
            throw new GenerateMessageException("WithdrawalStatusChanged error when generate webhookMessage!", e);
        }
    }

    private Long initPatenId(WebHookModel model, Long parentId) {
        if (model.getEventTypes() != null && model.getEventTypes().contains(EventType.WITHDRAWAL_CREATED)) {
            return parentId;
        }
        return parentIsNotExistId;
    }

    private String initRequestBody(WithdrawalStatus event, String withdrawalId) throws JsonProcessingException {
        if (event.isSetFailed()) {
            WithdrawalFailed withdrawalFailed = new WithdrawalFailed()
                    .withdrawalID(withdrawalId);
            return objectMapper.writeValueAsString(withdrawalFailed);
        } else if (event.isSetSucceeded()) {
            WithdrawalSucceeded withdrawalSucceeded = new WithdrawalSucceeded()
                    .withdrawalID(withdrawalId);
            return objectMapper.writeValueAsString(withdrawalSucceeded);
        } else {
            log.error("Unknown WithdrawalStatus status: {} withdrawalId: {}", event, withdrawalId);
            String message = String.format("Unknown WithdrawalStatus status: %s withdrawalId: %s", event, withdrawalId);
            throw new GenerateMessageException(message);
        }
    }

}
