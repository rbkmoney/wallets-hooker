package com.rbkmoney.wallets_hooker.service.crypt;

import com.rbkmoney.wallets_hooker.service.err.UnknownCryptoException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class AsymSigner implements Signer {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String HASH_ALGORITHM = "SHA256withRSA";
    public static final int KEYSIZE = 2048;

    private KeyFactory keyFactory;
    private Signature sig;
    private KeyPairGenerator keyGen;

    public AsymSigner() {
        try {
            this.keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            this.sig = Signature.getInstance(HASH_ALGORITHM);
            this.keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            this.keyGen.initialize(KEYSIZE);
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownCryptoException(e);
        }
    }

    @Override
    public String sign(String data, String secret) {
        try {
            KeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(secret));
            PrivateKey privateKey;
            synchronized (keyFactory) {
                privateKey = keyFactory.generatePrivate(privateKeySpec);
            }
            byte[] signatureBytes;
            synchronized (sig) {
                sig.initSign(privateKey);
                sig.update(data.getBytes(StandardCharsets.UTF_8));
                signatureBytes = sig.sign();
            }
            return Base64.getUrlEncoder().encodeToString(signatureBytes);
        } catch (InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            throw new UnknownCryptoException(e);
        }
    }

    @Override
    public KeyPair generateKeys() {
        java.security.KeyPair key;
        synchronized (keyGen) {
            key = keyGen.generateKeyPair();
        }
        return new KeyPair(
                Base64.getEncoder().encodeToString(key.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(key.getPublic().getEncoded()));
    }
}
