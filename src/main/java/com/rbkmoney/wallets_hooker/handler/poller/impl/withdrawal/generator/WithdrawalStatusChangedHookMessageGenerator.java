package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.withdrawal.WithdrawalStatus;
import com.rbkmoney.swag.wallets.webhook.events.model.Event;
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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

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
    public WebhookMessage generate(WithdrawalStatus event, WebHookModel model, String withdrawalId, Long eventId, Long parentId, String createdAt) {
        try {
            String message = initRequestBody(event, withdrawalId, eventId, createdAt);

            WebhookMessage webhookMessage = generatorService.generate(event, model, withdrawalId, eventId, parentId, createdAt);
            webhookMessage.setParentEventId(initPatenId(model, parentId));
            webhookMessage.setRequestBody(message.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, message));

            log.info("Webhook message from withdrawal_event_status_changed was generated, withdrawalId={}, statusChange={}, model={}", withdrawalId, event.toString(), model.toString());

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

    private String initRequestBody(WithdrawalStatus event, String withdrawalId, Long eventId, String createdAt) throws JsonProcessingException {
        if (event.isSetFailed()) {
            WithdrawalFailed withdrawalFailed = new WithdrawalFailed()
                    .withdrawalID(withdrawalId);
            withdrawalFailed.setEventType(Event.EventTypeEnum.WITHDRAWALFAILED);
            withdrawalFailed.setEventID(eventId.toString());
            withdrawalFailed.setOccuredAt(OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME));
            withdrawalFailed.setTopic(Event.TopicEnum.WITHDRAWALTOPIC);
            return objectMapper.writeValueAsString(withdrawalFailed);
        } else if (event.isSetSucceeded()) {
            WithdrawalSucceeded withdrawalSucceeded = new WithdrawalSucceeded()
                    .withdrawalID(withdrawalId);
            withdrawalSucceeded.setEventType(Event.EventTypeEnum.WITHDRAWALSUCCEEDED);
            withdrawalSucceeded.setEventID(eventId.toString());
            withdrawalSucceeded.setOccuredAt(OffsetDateTime.parse(createdAt));
            withdrawalSucceeded.setTopic(Event.TopicEnum.WITHDRAWALTOPIC);
            return objectMapper.writeValueAsString(withdrawalSucceeded);
        } else {
            log.error("Unknown WithdrawalStatus status: {} withdrawalId: {}", event, withdrawalId);
            String message = String.format("Unknown WithdrawalStatus status: %s withdrawalId: %s", event, withdrawalId);
            throw new GenerateMessageException(message);
        }
    }

}
