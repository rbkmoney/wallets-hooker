package com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.swag.wallets.webhook.events.model.Event;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationCreatedHookMessageGenerator implements HookMessageGenerator<DestinationMessage> {

    private final WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    @Value("${parent.not.exist.id}")
    private Long parentIsNotExistId;

    @Override
    public WebhookMessage generate(DestinationMessage event, WebHookModel model, String sourceId, Long eventId, String createdAt) {
        return generate(event, model, sourceId, eventId, parentIsNotExistId, createdAt);
    }

    @Override
    public WebhookMessage generate(DestinationMessage destinationMessage, WebHookModel model, String destinationId,
                                   Long eventId, Long parentId, String createdAt) {
        try {
            log.info("Start generating webhook message from destination event created, destinationId={}, model={}", destinationId, model.toString());

            Destination value = objectMapper.readValue(destinationMessage.getMessage(), Destination.class);
            value.setIdentity(model.getIdentityId());

            DestinationCreated destinationCreated = new DestinationCreated();
            destinationCreated.setDestination(value);
            destinationCreated.setEventID(eventId.toString());
            destinationCreated.setEventType(Event.EventTypeEnum.DESTINATIONCREATED);
            OffsetDateTime parse = OffsetDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
            destinationCreated.setOccuredAt(parse);
            destinationCreated.setTopic(Event.TopicEnum.DESTINATIONTOPIC);

            String requestBody = objectMapper.writeValueAsString(destinationCreated);

            WebhookMessage webhookMessage = generatorService.generate(destinationMessage, model, destinationId, eventId, parentId, createdAt);
            webhookMessage.setRequestBody(requestBody.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, requestBody));
            webhookMessage.setEventId(eventId);

            log.info("Finish generating webhook message from destination event created, destinationId={}, model={}", destinationId, model.toString());

            return webhookMessage;
        } catch (Exception e) {
            throw new GenerateMessageException(String.format("DestinationCreatedHookMessageGenerator error when generate, destinationMessage=%s model=%s", destinationMessage, model.toString()), e);
        }
    }

}
