package com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationResource;
import com.rbkmoney.wallets_hooker.configuration.MappingConfig;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.wallets_hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class DestinationCreatedHookMessageGeneratorTest {

    public static final long EVENT_ID = 1L;
    public static final String URL = "/url";
    public static final String WALLET_ID = "wallet_id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String TEST = "test";
    public static final String SOURCE_ID = "sourceId";
    public static final String DESTINATION_ID = "destination_id";

    ObjectMapper objectMapper = new MappingConfig().objectMapper();

    Signer signer = new AsymSigner();

    WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService = new WebHookMessageGeneratorServiceImpl<>();
    DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator =
            new DestinationCreatedHookMessageGenerator(
                    generatorService,
                    objectMapper,
                    new AdditionalHeadersGenerator(signer));

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

        DestinationMessage event = new DestinationMessage();
        Destination destination = new Destination();
        destination.setIdentity(IDENTITY_ID);
        destination.setId(DESTINATION_ID);
        destination.setResource(new DestinationResource().type(DestinationResource.TypeEnum.BANKCARD));
        destination.setCurrency("RUB");


        event.setMessage(objectMapper.writeValueAsString(destination));
        event.setDestinationId(DESTINATION_ID);

        String createdAt = "2019-07-02T08:43:42Z";

        WebhookMessage generate = destinationCreatedHookMessageGenerator.generate(event,
                model, SOURCE_ID, EVENT_ID, 0L, createdAt);

        System.out.println(generate);

        Assert.assertEquals(EVENT_ID, generate.getEventId());
        Assert.assertEquals(URL, generate.getUrl());
        Assert.assertEquals(ContentType.APPLICATION_JSON.getMimeType(), generate.getContentType());
        Assert.assertEquals(SOURCE_ID, generate.getSourceId());
        Assert.assertNotNull(generate.getAdditionalHeaders().get(AdditionalHeadersGenerator.SIGNATURE_HEADER));
        byte[] requestBody = generate.getRequestBody();

        DestinationCreated value = objectMapper.readValue(requestBody, DestinationCreated.class);
        Assert.assertNotNull(IDENTITY_ID, value.getDestination().getIdentity());

    }
}