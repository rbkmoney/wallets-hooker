package com.rbkmoney.wallets.hooker.converter;

import com.rbkmoney.fistful.base.*;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.msgpack.Value;
import org.junit.Test;

import static org.junit.Assert.*;

public class DestinationToDestinationMessageConverterTest {

    private DestinationToDestinationMessageConverter converter = new DestinationToDestinationMessageConverter();

    @Test
    public void testConvertFromEventWithBankCardResource() {
        Destination destination = new Destination()
                .setName("name")
                .setExternalId("external_id")
                .setResource(Resource.bank_card(
                        new ResourceBankCard(
                                new BankCard("token")
                                        .setBin("bin")
                                        .setMaskedPan("masked_pan")
                                        .setCardType(CardType.charge_card)
                                        .setBinDataId(Value.i(1))
                                        .setPaymentSystem(BankCardPaymentSystem.mastercard)
                        )
                ));
        var swagDestination = converter.convert(destination);
        assertEquals(destination.getExternalId(), swagDestination.getExternalID());
        assertEquals(destination.getName(), swagDestination.getName());
        var bankCard = (com.rbkmoney.swag.wallets.webhook.events.model.BankCard) swagDestination.getResource();
        assertNotNull(bankCard.getPaymentSystem());
    }

    @Test
    public void testConvertFromEventWithCryptoWalletResource() {
        Destination destination = new Destination()
                .setName("name")
                .setExternalId("external_id")
                .setResource(Resource.crypto_wallet(
                        new ResourceCryptoWallet(
                                new CryptoWallet("crypto_wallet_id", CryptoCurrency.bitcoin_cash)
                                        .setData(CryptoData.bitcoin_cash(new CryptoDataBitcoinCash()))
                        )
                ));
        var swagDestination = converter.convert(destination);
        assertEquals(destination.getExternalId(), swagDestination.getExternalID());
        assertEquals(destination.getName(), swagDestination.getName());
        var cryptoWallet = (com.rbkmoney.swag.wallets.webhook.events.model.CryptoWallet) swagDestination.getResource();
        assertNotNull(cryptoWallet.getCurrency());
    }

    @Test
    public void testConvertFromEventWithBankCardResourceAndPaymentSystemIsNull() {
        Destination destination = new Destination()
                .setName("name")
                .setExternalId("external_id")
                .setResource(Resource.bank_card(
                        new ResourceBankCard(
                                new BankCard("token")
                                        .setBin("bin")
                                        .setMaskedPan("masked_pan")
                                        .setCardType(CardType.charge_card)
                                        .setBinDataId(Value.i(1))
                                        .setPaymentSystem(null)
                        )
                ));
        var swagDestination = converter.convert(destination);
        assertEquals(destination.getExternalId(), swagDestination.getExternalID());
        assertEquals(destination.getName(), swagDestination.getName());
        var bankCard = (com.rbkmoney.swag.wallets.webhook.events.model.BankCard) swagDestination.getResource();
        assertNull(bankCard.getPaymentSystem());
    }

}
