package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.base.Cash;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.swag.wallets.webhook.events.model.Event;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalBody;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalStarted;
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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
            var withdrawal = new com.rbkmoney.swag.wallets.webhook.events.model.Withdrawal();
            withdrawal.setDestination(event.getDestination());
            withdrawal.setId(withdrawalId);
            withdrawal.setWallet(event.getSource());
            withdrawal.setBody(initBody(event));
            WithdrawalStarted withdrawalStarted = new WithdrawalStarted();
            withdrawalStarted.setWithdrawal(withdrawal);
            withdrawalStarted.setEventType(Event.EventTypeEnum.WITHDRAWALSTARTED);
            withdrawalStarted.setEventID(eventId.toString());
            withdrawalStarted.setOccuredAt(OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME));
            withdrawalStarted.setTopic(Event.TopicEnum.WITHDRAWALTOPIC);

            String requestBody = objectMapper.writeValueAsString(withdrawalStarted);

            WebhookMessage webhookMessage = generatorService.generate(event, model, withdrawalId, eventId, parentId, createdAt);
            webhookMessage.setRequestBody(requestBody.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, requestBody));
            webhookMessage.setParentEventId(parentId);

            log.info("Webhook message from withdrawal_event_created was generated, withdrawalId={}, model={}", withdrawalId, model.toString());

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
