package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.kafka.common.serialization.ThriftSerializer;
import com.rbkmoney.wallets_hooker.dao.identity.IdentityKeyDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.IdentityKey;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;

public class WebHookConverterTest {

    public static final String IDENTITY_ID = "1190";

    @Test
    public void convert() {
        IdentityKeyDao identityKeyDao = Mockito.mock(IdentityKeyDao.class);
        IdentityKey identityKey = new IdentityKey();
        identityKey.setIdentityId(IDENTITY_ID);
        identityKey.setId(1L);
        identityKey.setPrivKey("sasfsd");
        identityKey.setPubKey("xcvxc");
        Mockito.when(identityKeyDao.getByIdentity(any())).thenReturn(identityKey);
        WebHookConverter webHookConverter = new WebHookConverter(identityKeyDao);

        Webhook event = new Webhook();
        event.setIdentityId(IDENTITY_ID);
        event.setId(666L);
        event.setEnabled(true);
        event.setUrl("/");
        event.setWalletId("854");
        com.rbkmoney.fistful.webhooker.Webhook convert = webHookConverter.convert(event);

        ThriftSerializer<com.rbkmoney.fistful.webhooker.Webhook> webhookThriftSerializer = new ThriftSerializer<>();

        byte[] tests = webhookThriftSerializer.serialize("test", convert);

        Assert.assertTrue(tests.length > 0);
    }
}