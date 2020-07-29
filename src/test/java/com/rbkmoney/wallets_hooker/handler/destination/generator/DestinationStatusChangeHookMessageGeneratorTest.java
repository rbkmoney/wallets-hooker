package com.rbkmoney.wallets_hooker.handler.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.Authorized;
import com.rbkmoney.fistful.destination.Status;
import com.rbkmoney.fistful.destination.StatusChange;
import com.rbkmoney.fistful.destination.Unauthorized;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationAuthorized;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationUnauthorized;
import com.rbkmoney.wallets_hooker.config.ObjectMapperConfig;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets_hooker.handler.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.model.MessageGenParams;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.wallets_hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DestinationStatusChangeHookMessageGeneratorTest {

    public static final long EVENT_ID = 1L;
    public static final String URL = "/url";
    public static final String WALLET_ID = "wallet_id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String SOURCE_ID = "sourceId";
    public static final long PARENT_ID = 0L;
    public static final String T_08_43_42_Z = "2019-07-02T08:43:42Z";

    ObjectMapper objectMapper = new ObjectMapperConfig().objectMapper();

    Signer signer = new AsymSigner();

    WebHookMessageGeneratorServiceImpl<StatusChange> generatorService = new WebHookMessageGeneratorServiceImpl<>(PARENT_ID);
    DestinationStatusChangeHookMessageGenerator destinationCreatedHookMessageGenerator =
            new DestinationStatusChangeHookMessageGenerator(
                    generatorService,
                    objectMapper,
                    new AdditionalHeadersGenerator(signer),
                    -1L);

    @Test
    public void generate() throws IOException {
        WebHookModel model = new WebHookModel();
        model.setId(1L);
        model.setEventTypes(Set.of(EventType.DESTINATION_AUTHORIZED));
        model.setEnabled(true);
        model.setIdentityId(IDENTITY_ID);
        model.setUrl(URL);
        model.setWalletId(WALLET_ID);

        KeyPair keyPair = signer.generateKeys();
        model.setPrivateKey(keyPair.getPrivKey());
        model.setPubKey(keyPair.getPublKey());

        StatusChange statusChange = new StatusChange();
        statusChange.setChanged(Status.authorized(new Authorized()));

        MessageGenParams genParamAuth = MessageGenParams.builder()
                .sourceId(SOURCE_ID)
                .eventId(EVENT_ID)
                .parentId(0L)
                .createdAt("2019-07-02T08:43:42Z")
                .externalId("externalId")
                .build();
        WebhookMessage generate = destinationCreatedHookMessageGenerator.generate(statusChange, model, genParamAuth);

        byte[] requestBody = generate.getRequestBody();
        DestinationAuthorized destinationAuthorized = objectMapper.readValue(requestBody, DestinationAuthorized.class);
        assertEquals(SOURCE_ID, destinationAuthorized.getDestinationID());
        assertEquals("externalId", destinationAuthorized.getExternalID());

        statusChange = new StatusChange();
        statusChange.setChanged(Status.unauthorized(new Unauthorized()));

        MessageGenParams genParamUnauth = MessageGenParams.builder()
                .sourceId(SOURCE_ID)
                .eventId(EVENT_ID)
                .parentId(666L)
                .createdAt("2019-07-02T08:43:42Z")
                .externalId("externalId")
                .build();
        model.setEventTypes(Set.of(EventType.DESTINATION_AUTHORIZED, EventType.DESTINATION_CREATED));
        generate = destinationCreatedHookMessageGenerator.generate(statusChange, model, genParamUnauth);


        requestBody = generate.getRequestBody();
        DestinationUnauthorized destinationUnauthorized = objectMapper.readValue(requestBody, DestinationUnauthorized.class);
        assertEquals(SOURCE_ID, destinationUnauthorized.getDestinationID());
        assertEquals(666L, generate.getParentEventId());
        assertEquals("externalId", destinationUnauthorized.getExternalID());
    }

    @Test(expected = GenerateMessageException.class)
    public void generateException() {
        WebHookMessageGeneratorServiceImpl mock = Mockito.mock(WebHookMessageGeneratorServiceImpl.class);
        DestinationStatusChangeHookMessageGenerator destinationCreatedHookMessageGenerator =
                new DestinationStatusChangeHookMessageGenerator(
                        mock,
                        new ObjectMapper(),
                        new AdditionalHeadersGenerator(signer),
                        -1L);

        StatusChange event = new StatusChange();
        WebHookModel model = new WebHookModel();
        MessageGenParams genParam = MessageGenParams.builder()
                .sourceId(SOURCE_ID)
                .eventId(EVENT_ID)
                .parentId(PARENT_ID)
                .createdAt(T_08_43_42_Z)
                .build();
        when(mock.generate(event, model, genParam)).thenThrow(new RuntimeException("test exception!"));

        destinationCreatedHookMessageGenerator.generate(event, model, genParam);
    }
}
