package com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
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
public class DestinationCreatedHookMessageGenerator implements HookMessageGenerator<DestinationMessage> {

    private final WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    @Override
    public WebhookMessage generate(DestinationMessage destinationMessage, WebHookModel model, Long eventId, Long parentId) {
        try {
            WebhookMessage webhookMessage = generatorService.generate(destinationMessage, model, eventId, parentId);
            DestinationCreated destinationCreated = new DestinationCreated();
            Destination value = objectMapper.readValue(destinationMessage.getMessage(), Destination.class);
            value.setIdentity(model.getIdentityId());
            destinationCreated.setDestination(value);
            String requestBody = objectMapper.writeValueAsString(destinationCreated);
            webhookMessage.setRequestBody(requestBody.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, requestBody));
            webhookMessage.setEventId(eventId);
            webhookMessage.setParentEventId(0);
            log.info("Webhook message generated webhookMessage: {} for model: {}", webhookMessage, model);
            return webhookMessage;
        } catch (Exception e) {
            log.error("DestinationCreatedHookMessageGenerator error when generate destinationMessage: {} model: {} e: ", destinationMessage, model, e);
            throw new GenerateMessageException("DestinationCreatedHookMessageGenerator error when generate destinationMessage!", e);
        }
    }

}
