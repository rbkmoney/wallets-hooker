package com.rbkmoney.wallets_hooker.handler.poller.impl.destination.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.swag.wallets.webhook.events.model.Destination;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.wallets_hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import org.junit.Test;

import java.util.Set;

public class DestinationCreatedHookMessageGeneratorTest {

    public static final long EVENT_ID = 1L;

    Signer signer = new AsymSigner();

    WebHookMessageGeneratorServiceImpl<DestinationMessage> generatorService = new WebHookMessageGeneratorServiceImpl<>();
    DestinationCreatedHookMessageGenerator destinationCreatedHookMessageGenerator =
            new DestinationCreatedHookMessageGenerator(
                    generatorService,
                    new ObjectMapper(),
                    new AdditionalHeadersGenerator(signer));

    @Test
    public void generate() {
        WebHookModel model = new WebHookModel();
        model.setId(1L);
        model.setEventTypes(Set.of(EventType.DESTINATION_CREATED));
        model.setEnabled(true);
        model.setIdentityId("identity_id");
        model.setUrl("/url");
        model.setWalletId("wallet_id");
        model.setPubKey("test");

        KeyPair keyPair = signer.generateKeys();
        model.setPrivateKey(keyPair.getPrivKey());
        model.setPubKey(keyPair.getPublKey());

        DestinationMessage event = new DestinationMessage();
        Destination destination = new Destination();

        event.setMessage("{}");
        event.setDestinationId("destination_id");

        WebhookMessage generate = destinationCreatedHookMessageGenerator.generate(event,
                model, EVENT_ID, 0L);


    }
}