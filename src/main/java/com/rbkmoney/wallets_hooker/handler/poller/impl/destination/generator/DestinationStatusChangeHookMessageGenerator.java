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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationStatusChangeHookMessageGenerator implements HookMessageGenerator<StatusChange> {

    private final WebHookMessageGeneratorServiceImpl<StatusChange> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    @Value("${parent.not.exist.id}")
    private Long parentIsNotExistId;

    @Override
    public WebhookMessage generate(StatusChange event, WebHookModel model, Long eventId) {
        return generate(event, model, eventId, parentIsNotExistId);
    }

    @Override
    public WebhookMessage generate(StatusChange statusChange, WebHookModel model, Long eventId, Long parentId) {
        try {
            WebhookMessage webhookMessage = generatorService.generate(statusChange, model, eventId, parentId);
            webhookMessage.setParentEventId(initPatenId(model, parentId));
            String message = generateMessage(statusChange);
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

    private Long initPatenId(WebHookModel model, Long parentId) {
        if (model.getEventTypes().contains(EventType.DESTINATION_CREATED)) {
            return parentId;
        }
        return parentIsNotExistId;
    }

    private String generateMessage(StatusChange statusChange) throws JsonProcessingException {
        String message = "";
        if (statusChange.getChanged().isSetAuthorized()) {
                DestinationAuthorized destinationAuthorized = new DestinationAuthorized();
                message = objectMapper.writeValueAsString(destinationAuthorized);
        } else if (statusChange.getChanged().isSetUnauthorized()) {
                DestinationUnauthorized destination = new DestinationUnauthorized();
                message = objectMapper.writeValueAsString(destination);
        }
        return message;
    }
}
