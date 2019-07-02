package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.Resource;
import com.rbkmoney.swag.wallets.webhook.events.model.BankCard;
import com.rbkmoney.swag.wallets.webhook.events.model.CryptoWallet;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationResource;
import com.rbkmoney.wallets_hooker.exception.UnknownResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestinationToDestinationMessageConverter implements Converter<Destination, com.rbkmoney.swag.wallets.webhook.events.model.Destination> {

    @Override
    public com.rbkmoney.swag.wallets.webhook.events.model.Destination convert(Destination event) {
        com.rbkmoney.swag.wallets.webhook.events.model.Destination destination = new com.rbkmoney.swag.wallets.webhook.events.model.Destination();
        destination.setExternalID(event.getExternalId());
        destination.setId(event.getId());
        destination.setMetadata(event.getFieldMetaData());
        destination.setName(event.getName());
        DestinationResource destinationResource = initDestinationResource(event.getResource());
        destination.setResource(destinationResource);
        return destination;
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
            throw new UnknownResourceException("Can't init destination with unknown resource");
        }
        return destinationResource;
    }
}