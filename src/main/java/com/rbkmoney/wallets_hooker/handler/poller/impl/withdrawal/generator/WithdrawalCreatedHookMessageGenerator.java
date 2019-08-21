package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.base.Cash;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalBody;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
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
public class WithdrawalCreatedHookMessageGenerator implements HookMessageGenerator<Withdrawal> {

    private final WebHookMessageGeneratorServiceImpl<Withdrawal> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    @Value("${parent.not.exist.id}")
    private Long parentIsNotExistId;

    @Override
    public WebhookMessage generate(Withdrawal event, WebHookModel model, String sourceId, Long eventId, String createdAt) {
        return generate(event, model, sourceId, eventId, parentIsNotExistId, createdAt);
    }

    @Override
    public WebhookMessage generate(Withdrawal event, WebHookModel model, String withdrawalId, Long eventId, Long parentId, String createdAt) {
        try {
            WebhookMessage webhookMessage = generatorService.generate(event, model, withdrawalId, eventId, parentId, createdAt);
            com.rbkmoney.swag.wallets.webhook.events.model.Withdrawal withdrawal = new com.rbkmoney.swag.wallets.webhook.events.model.Withdrawal();
            withdrawal.setDestination(event.getDestination());
            withdrawal.setId(withdrawalId);
            withdrawal.setWallet(event.getSource());
            withdrawal.setBody(initBody(event));
            String requestBody = objectMapper.writeValueAsString(withdrawal);
            webhookMessage.setRequestBody(requestBody.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, requestBody));
            webhookMessage.setParentEventId(parentId);
            log.info("Webhook message generated webhookMessage: {} for model: {}", webhookMessage, model);
            return webhookMessage;
        } catch (JsonProcessingException e) {
            log.error("Error when generate webhookMessage event: {} model: {} eventId: {} e: ", event, model, eventId, e);
            throw new GenerateMessageException("WithdrawalCreated error when generate webhookMessage!", e);
        }
    }

    private WithdrawalBody initBody(Withdrawal event) {
        Cash body = event.getBody();
        WithdrawalBody withdrawalBody = new WithdrawalBody();
        withdrawalBody.setAmount(body.getAmount());
        withdrawalBody.setCurrency(body.getCurrency().getSymbolicCode());
        return withdrawalBody;
    }

}
