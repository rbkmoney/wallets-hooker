package com.rbkmoney.wallets.hooker.handler.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationResource;
import com.rbkmoney.wallets.hooker.config.ObjectMapperConfig;
import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.handler.AdditionalHeadersGenerator;
import com.rbkmoney.wallets.hooker.model.MessageGenParams;
import com.rbkmoney.wallets.hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.wallets.hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets.hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets.hooker.service.crypt.Signer;
import com.rbkmoney.wallets.hooker.domain.enums.EventType;
import com.rbkmoney.wallets.hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DestinationCreatedHookMessageGeneratorTest {

    public static final long EVENT_ID = 1L;
    public static final String URL = "/url";
    public static final String WALLET_ID = "wallet_id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String SOURCE_ID = "sourceId";
    public static final String DESTINATION_ID = "destination_id";

    ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();

    Signer signer = new AsymSigner();

    WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService =
            new WebHookMessageGeneratorServiceImpl<>(-1L);
    DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator =
            new DestinationCreatedHookMessageGenerator(
                    generatorService,
                    objectMapper,
                    new AdditionalHeadersGenerator(signer),
                    -1L);

    @Test
    public void generate() throws IOException {
        WebHookModel model = new WebHookModel();
        model.setId(1L);
        model.setEventTypes(Set.of(EventType.DESTINATION_CREATED));
        model.setEnabled(true);
        model.setIdentityId(IDENTITY_ID);
        model.setUrl(URL);
        model.setWalletId(WALLET_ID);

        KeyPair keyPair = signer.generateKeys();
        model.setPrivateKey(keyPair.getPrivKey());
        model.setPubKey(keyPair.getPublKey());

        Destination destination = new Destination();
        destination.setIdentity(IDENTITY_ID);
        destination.setId(DESTINATION_ID);
        destination.setResource(new DestinationResource().type(DestinationResource.TypeEnum.BANKCARD));
        destination.setCurrency("RUB");

        DestinationMessage event = new DestinationMessage();
        event.setMessage(objectMapper.writeValueAsString(destination));
        event.setDestinationId(DESTINATION_ID);

        String createdAt = "2019-07-02T08:43:42Z";

        MessageGenParams genParam = MessageGenParams.builder()
                .sourceId(SOURCE_ID)
                .eventId(EVENT_ID)
                .parentId(0L)
                .createdAt(createdAt)
                .build();
        WebhookMessage generate = destinationCreatedHookMessageGenerator.generate(event, model, genParam);

        System.out.println(generate);

        assertEquals(EVENT_ID, generate.getEventId());
        assertEquals(URL, generate.getUrl());
        assertEquals(ContentType.APPLICATION_JSON.getMimeType(), generate.getContentType());
        assertEquals(SOURCE_ID, generate.getSourceId());
        assertNotNull(generate.getAdditionalHeaders().get(AdditionalHeadersGenerator.SIGNATURE_HEADER));
        byte[] requestBody = generate.getRequestBody();

        DestinationCreated value = objectMapper.readValue(requestBody, DestinationCreated.class);
        assertNotNull(IDENTITY_ID, value.getDestination().getIdentity());
    }
}
