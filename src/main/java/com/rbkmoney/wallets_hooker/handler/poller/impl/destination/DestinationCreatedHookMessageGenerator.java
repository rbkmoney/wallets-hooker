package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationCreatedHookMessageGenerator implements HookMessageGenerator<DestinationMessage> {

    private final WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService;
    private final ObjectMapper objectMapper;

    @Override
    public WebhookMessage generate(DestinationMessage destinationMessage, WebHookModel model, Long eventId, Long parentId) {
        WebhookMessage webhookMessage = generatorService.generate(destinationMessage, model, eventId, parentId);
        DestinationCreated destinationCreated = new DestinationCreated();
        try {
            Destination value = objectMapper.readValue(destinationMessage.getMessage(), Destination.class);
            value.setIdentity(model.getIdentityId());
            destinationCreated.setDestination(value);
            webhookMessage.setRequestBody(objectMapper.writeValueAsBytes(destinationCreated));
        } catch (IOException e) {
            log.error("DestinationCreatedHookMessageGenerator error when generate destinationMessage: {} model: {} e: ", destinationMessage, model, e);
            throw new RuntimeException("DestinationCreatedHookMessageGenerator error when generate destinationMessage!", e);
        }
        webhookMessage.setEventId(eventId);
        webhookMessage.setParentEventId(0);
        return webhookMessage;
    }

}
