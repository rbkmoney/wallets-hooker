package com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.StatusChange;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationAuthorized;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationUnauthorized;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DestinationStatusChangeHookMessageGenerator implements HookMessageGenerator<StatusChange> {

    private final WebHookMessageGeneratorServiceImpl<StatusChange> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;
    private Long parentIsNotExistId;

    public DestinationStatusChangeHookMessageGenerator(WebHookMessageGeneratorServiceImpl<StatusChange> generatorService,
                                                       ObjectMapper objectMapper, AdditionalHeadersGenerator additionalHeadersGenerator,
                                                       @Value("${parent.not.exist.id}") Long parentIsNotExistId) {
        this.generatorService = generatorService;
        this.objectMapper = objectMapper;
        this.additionalHeadersGenerator = additionalHeadersGenerator;
        this.parentIsNotExistId = parentIsNotExistId;
    }

    @Override
    public WebhookMessage generate(StatusChange event, WebHookModel model, String sourceId, Long eventId, String createdAt) {
        return generate(event, model, sourceId, eventId, parentIsNotExistId, createdAt);
    }

    @Override
    public WebhookMessage generate(StatusChange statusChange, WebHookModel model, String sourceId, Long eventId, Long parentId, String createdAt) {
        try {
            WebhookMessage webhookMessage = generatorService.generate(statusChange, model, sourceId, eventId, parentId, createdAt);
            webhookMessage.setParentEventId(initParentId(model, parentId));
            String message = generateMessage(statusChange, sourceId);
            additionalHeadersGenerator.generate(model, message);
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, message));
            webhookMessage.setRequestBody(message.getBytes());
            log.info("Webhook message generated webhookMessage: {} for model: {}", webhookMessage, model);
            return webhookMessage;
        } catch (Exception e) {
            log.error("Error when generate webhookMessage e: ", e);
            throw new GenerateMessageException("Error when generate webhookMessage", e);
        }
    }

    private Long initParentId(WebHookModel model, Long parentId) {
        if (model.getEventTypes().contains(EventType.DESTINATION_CREATED)) {
            return parentId;
        }
        return parentIsNotExistId;
    }

    private String generateMessage(StatusChange statusChange, String destinationId) throws JsonProcessingException {
        if (statusChange.getChanged().isSetAuthorized()) {
            DestinationAuthorized destination = new DestinationAuthorized();
            destination.setDestinationID(destinationId);
            return objectMapper.writeValueAsString(destination);
        } else if (statusChange.getChanged().isSetUnauthorized()) {
            DestinationUnauthorized destination = new DestinationUnauthorized();
            destination.setDestinationID(destinationId);
            return objectMapper.writeValueAsString(destination);
        } else {
            log.error("Unknown statusChange: {}", statusChange);
            throw new GenerateMessageException("Unknown statusChange!");
        }
    }
}
