package com.rbkmoney.wallets_hooker.handler.poller.impl;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.handler.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class AdditionalHeadersGeneratorTest {

    AsymSigner signer = new AsymSigner();
    AdditionalHeadersGenerator additionalHeadersGenerator = new AdditionalHeadersGenerator(signer);

    @Test
    public void generate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair keyPair = signer.generateKeys();
        WebHookModel model = new WebHookModel();
        model.setPrivateKey(keyPair.getPrivKey());
        Map<String, String> map = additionalHeadersGenerator.generate(model, "testString");

        String signature = map.get(AdditionalHeadersGenerator.SIGNATURE_HEADER);

        Assert.assertNotNull(signature);
    }
}