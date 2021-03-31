package com.rbkmoney.wallets.hooker.service.crypt;

import org.junit.Test;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AsymSignerTest {
    @Test
    public void test() throws Exception {
        AsymSigner asymSigner = new AsymSigner();
        KeyPair keyPair = asymSigner.generateKeys();
        String data = "{\"eventID\":27,\"occuredAt\":\"2017-05-16T13:49:34.935099Z\",\"topic\":\"InvoicesTopic\",\"eventType\":\"PaymentCaptured\",\"invoice\":{\"id\":\"qXMiygTqb2\",\"shopID\":1,\"createdAt\":\"2017-05-16T13:49:32.753723Z\",\"status\":\"unpaid\",\"reason\":null,\"dueDate\":\"2017-05-16T13:59:32Z\",\"amount\":100000,\"currency\":\"RUB\",\"metadata\":{\"retryPolicyType\":\"application/json\",\"data\":\"eyJpbnZvaWNlX2R1bW15X2NvbnRleHQiOiJ0ZXN0X3ZhbHVlIn0=\"},\"product\":\"test_product\",\"description\":\"test_invoice_description\"},\"payment\":{\"id\":\"1\",\"createdAt\":\"2017-05-16T13:49:33.182195Z\",\"status\":\"captured\",\"error\":null,\"amount\":100000,\"currency\":\"RUB\",\"paymentToolToken\":\"5Gz2nhE1eleFGBAcGe9SrA\",\"paymentSession\":\"2nTYVgk6h85O7vIVV9j4pA\",\"contactInfo\":{\"email\":\"bla@bla.ru\",\"phoneNumber\":null},\"ip\":\"10.100.2.1\",\"fingerprint\":\"test fingerprint\"}}";
        String sign = asymSigner.sign(data, keyPair.getPrivKey());
        byte[] sigBytes = Base64.getUrlDecoder().decode(sign);

        byte[] publicBytes = Base64.getDecoder().decode(keyPair.getPublKey());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(AsymSigner.KEY_ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature1 = Signature.getInstance(AsymSigner.HASH_ALGORITHM);
        signature1.initVerify(pubKey);
        signature1.update(data.getBytes());
        assertTrue(signature1.verify(sigBytes));

        Signature signature2 = Signature.getInstance(AsymSigner.HASH_ALGORITHM);
        signature2.initVerify(pubKey);
        signature2.update("other text".getBytes());
        assertFalse(signature2.verify(sigBytes));
    }
}
