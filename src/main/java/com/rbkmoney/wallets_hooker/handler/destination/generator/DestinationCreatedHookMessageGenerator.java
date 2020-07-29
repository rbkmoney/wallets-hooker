package com.rbkmoney.wallets_hooker.handler.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.swag.wallets.webhook.events.model.Event;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.MessageGenParams;
import com.rbkmoney.wallets_hooker.service.BaseHookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DestinationCreatedHookMessageGenerator extends BaseHookMessageGenerator<DestinationMessage> {

    private final WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    public DestinationCreatedHookMessageGenerator(WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService,
                                                  ObjectMapper objectMapper,
                                                  AdditionalHeadersGenerator additionalHeadersGenerator,
                                                  @Value("${parent.not.exist.id}") Long parentId) {
        super(parentId);
        this.generatorService = generatorService;
        this.objectMapper = objectMapper;
        this.additionalHeadersGenerator = additionalHeadersGenerator;
    }

    @Override
    protected WebhookMessage generateMessage(DestinationMessage event, WebHookModel model, MessageGenParams messageGenParams) {
        try {
            Destination value = objectMapper.readValue(event.getMessage(), Destination.class);
            value.setIdentity(model.getIdentityId());

            DestinationCreated destinationCreated = new DestinationCreated();
            destinationCreated.setDestination(value);
            destinationCreated.setEventID(messageGenParams.getEventId().toString());
            destinationCreated.setEventType(Event.EventTypeEnum.DESTINATIONCREATED);
            OffsetDateTime parse = OffsetDateTime.parse(messageGenParams.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME);
            destinationCreated.setOccuredAt(parse);
            destinationCreated.setTopic(Event.TopicEnum.DESTINATIONTOPIC);

            String requestBody = objectMapper.writeValueAsString(destinationCreated);

            WebhookMessage webhookMessage = generatorService.generate(event, model, messageGenParams);
            webhookMessage.setRequestBody(requestBody.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, requestBody));
            webhookMessage.setEventId(messageGenParams.getEventId());

            log.info("Webhook message from destination_event_created was generated, destinationId={}, model={}", messageGenParams.getSourceId(), model.toString());

            return webhookMessage;
        } catch (Exception e) {
            log.error("DestinationCreatedHookMessageGenerator error when generate destinationMessage: {} model: {} e: ", event, model.toString(), e);
            throw new GenerateMessageException("DestinationCreatedHookMessageGenerator error when generate destinationMessage!", e);
        }

    }
}
