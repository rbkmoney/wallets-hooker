package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.Resource;
import com.rbkmoney.swag.wallets.webhook.events.model.BankCard;
import com.rbkmoney.swag.wallets.webhook.events.model.CryptoWallet;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationCreated;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationResource;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationCreatedHookMessageGenerator implements HookMessageGenerator<Destination> {

    private final WebHookMessageGeneratorServiceImpl<Destination> generatorService;
    private final ObjectMapper objectMapper;

    @Override
    public WebhookMessage generate(Destination event, WebHookModel model, Long eventId, Long parentId) {
        WebhookMessage webhookMessage = generatorService.generate(event, model, eventId, parentId);
        webhookMessage.setCreatedAt(event.getCreatedAt());
        DestinationCreated destinationCreated = new DestinationCreated();
        com.rbkmoney.swag.wallets.webhook.events.model.Destination destination = new com.rbkmoney.swag.wallets.webhook.events.model.Destination();
        destination.setCurrency(event.getAccount().getCurrency().symbolic_code);
        destination.setExternalID(event.getExternalId());
        destination.setId(event.getId());
        destination.setIdentity(event.getAccount().getIdentity());
        destination.setMetadata(event.getFieldMetaData());
        destination.setName(event.getName());
        DestinationResource destinationResource = initDestinationResource(event.getResource());
        destination.setResource(destinationResource);
        destinationCreated.setDestination(destination);
        try {
            webhookMessage.setRequestBody(objectMapper.writeValueAsBytes(destinationCreated));
        } catch (JsonProcessingException e) {
            log.error("DestinationCreatedHookMessageGenerator error when generate event: {} model: {} e: ", event, model, e);
        }
        webhookMessage.setEventId(eventId);
        webhookMessage.setParentEventId(0);
        return webhookMessage;
    }

    private DestinationResource initDestinationResource(Resource resource) {
        DestinationResource destinationResource = null;
        if (resource.isSetBankCard()) {
            BankCard bankCard = new BankCard();
            bankCard.bin(resource.getBankCard().bin);
            bankCard.cardNumberMask(resource.getBankCard().masked_pan);
            bankCard.paymentSystem(BankCard.PaymentSystemEnum.fromValue(resource.getBankCard().payment_system.name()));
            destinationResource = bankCard;
        } else if (resource.isSetCryptoWallet()) {
            CryptoWallet cryptoWallet = new CryptoWallet();
            cryptoWallet.setCryptoWalletId(resource.getCryptoWallet().id);
            cryptoWallet.setCurrency(CryptoWallet.CurrencyEnum.fromValue(resource.getCryptoWallet().currency.name()));
            destinationResource = cryptoWallet;
        } else {
            throw new RuntimeException("");
        }
        return destinationResource;
    }

}
