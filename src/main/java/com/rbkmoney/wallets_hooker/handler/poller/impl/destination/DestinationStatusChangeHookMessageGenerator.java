package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.StatusChange;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationAuthorized;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationUnauthorized;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationStatusChangeHookMessageGenerator implements HookMessageGenerator<StatusChange> {

    private final WebHookMessageGeneratorServiceImpl<StatusChange> generatorService;
    private final ObjectMapper objectMapper;

    @Override
    public WebhookMessage generate(StatusChange statusChange, WebHookModel model, Long eventId, Long parentId) {
        WebhookMessage webhookMessage = generatorService.generate(statusChange, model, eventId, parentId);
        if (model.getEventTypes().contains(EventType.DESTINATION_CREATED)) {
            webhookMessage.setParentEventId(parentId);
        } else {
            webhookMessage.setParentEventId(0);
        }
        webhookMessage.setRequestBody(generateMessage(statusChange));
        return webhookMessage;
    }

    private byte[] generateMessage(StatusChange statusChange) {
        byte[] message = null;
        try {
            if (statusChange.getChanged().isSetAuthorized()) {
                DestinationAuthorized destinationAuthorized = new DestinationAuthorized();
                message = objectMapper.writeValueAsBytes(destinationAuthorized);
            } else if (statusChange.getChanged().isSetUnauthorized()) {
                DestinationUnauthorized destination = new DestinationUnauthorized();
                message = objectMapper.writeValueAsBytes(destination);
            }
        } catch (JsonProcessingException e) {
            log.error("Error when generate webhookMessage e: ", e);
        }
        return message;
    }
}
