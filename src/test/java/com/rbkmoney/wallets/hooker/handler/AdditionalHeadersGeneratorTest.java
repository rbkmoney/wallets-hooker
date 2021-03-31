package com.rbkmoney.wallets.hooker.handler;

import com.rbkmoney.wallets.hooker.domain.WebHookModel;
import com.rbkmoney.wallets.hooker.service.crypt.AsymSigner;
import com.rbkmoney.wallets.hooker.service.crypt.KeyPair;
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