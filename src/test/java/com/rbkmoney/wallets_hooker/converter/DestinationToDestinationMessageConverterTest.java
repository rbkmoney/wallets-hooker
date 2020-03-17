package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.fistful.account.Account;
import com.rbkmoney.fistful.base.*;
import com.rbkmoney.fistful.destination.Destination;
import com.rbkmoney.fistful.destination.Resource;
import com.rbkmoney.fistful.msgpack.Value;
import org.junit.Test;

import static org.junit.Assert.*;

public class DestinationToDestinationMessageConverterTest {

    private DestinationToDestinationMessageConverter converter = new DestinationToDestinationMessageConverter();

    @Test
    public void testConvertFromEventWithBankCardResource() {
        Destination destination = new Destination()
                .setId("id")
                .setName("name")
                .setExternalId("external_id")
                .setAccount(
                        new Account()
                                .setIdentity("identity")
                                .setCurrency(new CurrencyRef("RUB"))
                )
                .setResource(Resource.bank_card(
                        new BankCard("token")
                                .setBin("bin")
                                .setMaskedPan("masked_pan")
                                .setCardType(CardType.charge_card)
                                .setBinDataId(Value.i(1))
                                .setPaymentSystem(BankCardPaymentSystem.mastercard)
                ));
        var swagDestination = converter.convert(destination);
        assertEquals(destination.getId(), swagDestination.getId());
        assertEquals(destination.getExternalId(), swagDestination.getExternalID());
        assertEquals(destination.getName(), swagDestination.getName());
        assertEquals(destination.getAccount().getIdentity(), swagDestination.getIdentity());
        assertEquals(destination.getAccount().getCurrency().getSymbolicCode(), swagDestination.getCurrency());
        var bankCard = (com.rbkmoney.swag.wallets.webhook.events.model.BankCard) swagDestination.getResource();
        assertNotNull(bankCard.getPaymentSystem());
    }

    @Test
    public void testConvertFromEventWithCryptoWalletResource() {
        Destination destination = new Destination()
                .setId("id")
                .setName("name")
                .setExternalId("external_id")
                .setAccount(
                        new Account()
                                .setIdentity("identity")
                                .setCurrency(new CurrencyRef("RUB"))
                )
                .setResource(Resource.crypto_wallet(
                        new CryptoWallet("crypto_wallet_id", CryptoCurrency.bitcoin_cash)
                        .setData(CryptoData.bitcoin_cash(new CryptoDataBitcoinCash()))
                ));
        var swagDestination = converter.convert(destination);
        assertEquals(destination.getId(), swagDestination.getId());
        assertEquals(destination.getExternalId(), swagDestination.getExternalID());
        assertEquals(destination.getName(), swagDestination.getName());
        assertEquals(destination.getAccount().getIdentity(), swagDestination.getIdentity());
        assertEquals(destination.getAccount().getCurrency().getSymbolicCode(), swagDestination.getCurrency());
        var cryptoWallet = (com.rbkmoney.swag.wallets.webhook.events.model.CryptoWallet) swagDestination.getResource();
        assertNotNull(cryptoWallet.getCurrency());
    }

}
