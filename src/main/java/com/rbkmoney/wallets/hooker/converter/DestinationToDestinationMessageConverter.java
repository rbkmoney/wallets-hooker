package com.rbkmoney.wallets.hooker.converter;

import com.google.common.base.CaseFormat;
import com.rbkmoney.fistful.base.CryptoData;
import com.rbkmoney.fistful.base.Resource;
import com.rbkmoney.fistful.base.ResourceBankCard;
import com.rbkmoney.fistful.base.ResourceCryptoWallet;
import com.rbkmoney.fistful.base.ResourceDigitalWallet;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.mamsel.PaymentSystemUtil;
import com.rbkmoney.swag.wallets.webhook.events.model.BankCard;
import com.rbkmoney.swag.wallets.webhook.events.model.CryptoWallet;
import com.rbkmoney.swag.wallets.webhook.events.model.DestinationResource;
import com.rbkmoney.swag.wallets.webhook.events.model.DigitalWallet;
import com.rbkmoney.wallets.hooker.exception.UnknownResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DestinationToDestinationMessageConverter
        implements Converter<Destination, com.rbkmoney.swag.wallets.webhook.events.model.Destination> {

    @Override
    public com.rbkmoney.swag.wallets.webhook.events.model.Destination convert(Destination event) {
        var destination = new com.rbkmoney.swag.wallets.webhook.events.model.Destination();
        destination.setExternalID(event.getExternalId());
        destination.setName(event.getName());
        DestinationResource destinationResource = initDestinationResource(event.getResource());
        destination.setResource(destinationResource);
        // todo metadata null?
        destination.setMetadata(null);

        log.info("destinationDamsel has been converted, destination={}", destination.toString());

        return destination;
    }

    private DestinationResource initDestinationResource(Resource resource) {
        switch (resource.getSetField()) {
            case BANK_CARD:
                BankCard bankCard = new BankCard();
                ResourceBankCard resourceBankCard = resource.getBankCard();
                bankCard.setType(DestinationResource.TypeEnum.BANKCARD);
                bankCard.bin(resourceBankCard.getBankCard().bin);
                bankCard.cardNumberMask(resourceBankCard.getBankCard().masked_pan);
                bankCard.paymentSystem(
                        BankCard.PaymentSystemEnum.fromValue(
                                Objects.toString(
                                        PaymentSystemUtil.getFistfulPaymentSystemName(resourceBankCard.getBankCard()),
                                        null)
                        )
                );
                return bankCard;
            case CRYPTO_WALLET:
                CryptoWallet cryptoWallet = new CryptoWallet();
                cryptoWallet.setType(DestinationResource.TypeEnum.CRYPTOWALLET);
                ResourceCryptoWallet resourceCryptoWallet = resource.getCryptoWallet();
                cryptoWallet.setCryptoWalletId(resourceCryptoWallet.getCryptoWallet().id);
                if (resourceCryptoWallet.getCryptoWallet().isSetData()) {
                    CryptoData cryptoData = resourceCryptoWallet.getCryptoWallet().getData();
                    cryptoWallet.setCurrency(
                            CryptoWallet.CurrencyEnum.fromValue(
                                    CaseFormat.UPPER_UNDERSCORE.to(
                                            CaseFormat.UPPER_CAMEL,
                                            cryptoData.getSetField().getFieldName()
                                    )
                            )
                    );
                }
                return cryptoWallet;
            case DIGITAL_WALLET:
                DigitalWallet digitalWallet = new DigitalWallet();
                ResourceDigitalWallet resourceDigitalWallet = resource.getDigitalWallet();
                digitalWallet.setDigitalWalletId(resourceDigitalWallet.getDigitalWallet().getId());
                if (resourceDigitalWallet.getDigitalWallet().isSetData()) {
                    DigitalWallet.DigitalWalletProviderEnum digitalWalletProviderEnum = DigitalWallet
                            .DigitalWalletProviderEnum.fromValue(
                                    resourceDigitalWallet.getDigitalWallet().getData().getSetField().getFieldName());
                    digitalWallet.setDigitalWalletProvider(digitalWalletProviderEnum);
                }
                return digitalWallet;
            default:
                throw new UnknownResourceException("Can't init destination with unknown resource");
        }
    }
}
