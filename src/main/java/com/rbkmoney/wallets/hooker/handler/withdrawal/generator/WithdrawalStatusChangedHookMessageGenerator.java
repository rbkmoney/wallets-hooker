package com.rbkmoney.wallets.hooker.handler.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.withdrawal.StatusChange;
import com.rbkmoney.fistful.withdrawal.status.Status;
import com.rbkmoney.swag.wallets.webhook.events.model.Event;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalFailed;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalSucceeded;
import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.domain.enums.EventType;
import com.rbkmoney.wallets.hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets.hooker.handler.AdditionalHeadersGenerator;
import com.rbkmoney.wallets.hooker.model.MessageGenParams;
import com.rbkmoney.wallets.hooker.service.BaseHookMessageGenerator;
import com.rbkmoney.wallets.hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class WithdrawalStatusChangedHookMessageGenerator extends BaseHookMessageGenerator<StatusChange> {

    private final WebHookMessageGeneratorServiceImpl<StatusChange> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    public WithdrawalStatusChangedHookMessageGenerator(
            WebHookMessageGeneratorServiceImpl<StatusChange> generatorService,
            ObjectMapper objectMapper,
            AdditionalHeadersGenerator additionalHeadersGenerator,
            @Value("${parent.not.exist.id}") Long parentId) {
        super(parentId);
        this.generatorService = generatorService;
        this.objectMapper = objectMapper;
        this.additionalHeadersGenerator = additionalHeadersGenerator;
    }

    @Override
    protected WebhookMessage generateMessage(
            StatusChange event,
            WebHookModel model,
            MessageGenParams messageGenParams) {
        try {
            String message = initRequestBody(
                    event.getStatus(),
                    messageGenParams.getSourceId(),
                    messageGenParams.getEventId(),
                    messageGenParams.getCreatedAt(),
                    messageGenParams.getExternalId());

            WebhookMessage webhookMessage = generatorService.generate(event, model, messageGenParams);
            webhookMessage.setParentEventId(initPatenId(model, messageGenParams.getParentId()));
            webhookMessage.setRequestBody(message.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, message));

            log.info(
                    "Webhook message from withdrawal_event_status_changed was generated, " +
                            "withdrawalId={}, statusChange={}, model={}, body={}, externalId={}",
                    messageGenParams.getSourceId(), event.toString(), model.toString(), message,
                    messageGenParams.getExternalId());

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

        return super.parentIsNotExistId;
    }

    private String initRequestBody(
            Status status,
            String withdrawalId,
            Long eventId,
            String createdAt,
            String externalId) throws JsonProcessingException {
        if (status.isSetFailed()) {
            WithdrawalFailed withdrawalFailed = new WithdrawalFailed()
                    .withdrawalID(withdrawalId)
                    .externalID(externalId);
            withdrawalFailed.setEventType(Event.EventTypeEnum.WITHDRAWALFAILED);
            withdrawalFailed.setEventID(eventId.toString());
            withdrawalFailed.setOccuredAt(OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME));
            withdrawalFailed.setTopic(Event.TopicEnum.WITHDRAWALTOPIC);
            return objectMapper.writeValueAsString(withdrawalFailed);
        } else if (status.isSetSucceeded()) {
            WithdrawalSucceeded withdrawalSucceeded = new WithdrawalSucceeded()
                    .withdrawalID(withdrawalId)
                    .externalID(externalId);
            withdrawalSucceeded.setEventType(Event.EventTypeEnum.WITHDRAWALSUCCEEDED);
            withdrawalSucceeded.setEventID(eventId.toString());
            withdrawalSucceeded.setOccuredAt(OffsetDateTime.parse(createdAt));
            withdrawalSucceeded.setTopic(Event.TopicEnum.WITHDRAWALTOPIC);
            return objectMapper.writeValueAsString(withdrawalSucceeded);
        } else {
            log.error("Unknown WithdrawalStatus status: {} withdrawalId: {}", status, withdrawalId);
            String message = String.format(
                    "Unknown WithdrawalStatus status: %s withdrawalId: %s",
                    status, withdrawalId);
            throw new GenerateMessageException(message);
        }
    }

}
