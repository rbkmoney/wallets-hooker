package com.rbkmoney.wallets_hooker.handler.poller.impl;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.handler.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class AdditionalHeadersGeneratorTest {

    AsymSigner signer = new AsymSigner();
    AdditionalHeadersGenerator additionalHeadersGenerator = new AdditionalHeadersGenerator(signer);

    @Test
    public void generate() {
        KeyPair keyPair = signer.generateKeys();
        WebHookModel model = new WebHookModel();
        model.setPrivateKey(keyPair.getPrivKey());
        Map<String, String> map = additionalHeadersGenerator.generate(model, "testString");

        String signature = map.get(AdditionalHeadersGenerator.SIGNATURE_HEADER);

        assertNotNull(signature);
    }
}