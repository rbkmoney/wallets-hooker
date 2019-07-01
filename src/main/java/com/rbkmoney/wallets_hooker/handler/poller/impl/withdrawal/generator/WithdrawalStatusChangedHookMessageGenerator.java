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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalStatusChangedHookMessageGenerator implements HookMessageGenerator<WithdrawalStatus> {

    private final WebHookMessageGeneratorServiceImpl<WithdrawalStatus> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    @Override
    public WebhookMessage generate(WithdrawalStatus event, WebHookModel model, Long eventId, Long parentId) {
        try {
            WebhookMessage webhookMessage = generatorService.generate(event, model, eventId, parentId);
            if (model.getEventTypes() != null && model.getEventTypes().contains(EventType.WITHDRAWAL_CREATED)) {
                webhookMessage.setParentEventId(parentId);
            } else {
                webhookMessage.setParentEventId(0);
            }
            String message = initRequestBody(event);
            webhookMessage.setRequestBody(message.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, message));
            log.info("Webhook message generated webhookMessage: {} for model: {}", webhookMessage, model);
            return webhookMessage;
        } catch (Exception e) {
            log.error("Error when generate webhookMessage e: ", e);
            throw new GenerateMessageException("WithdrawalStatusChanged error when generate webhookMessage!", e);
        }
    }

    private String initRequestBody(WithdrawalStatus event) throws JsonProcessingException {
        String message = "";
        if (event.isSetFailed()) {
                WithdrawalFailed withdrawalFailed = new WithdrawalFailed();
                message = objectMapper.writeValueAsString(withdrawalFailed);
            } else if (event.isSetSucceeded()) {
                WithdrawalSucceeded withdrawalSucceeded = new WithdrawalSucceeded();
                message = objectMapper.writeValueAsString(withdrawalSucceeded);
            }
        return message;
    }

}
