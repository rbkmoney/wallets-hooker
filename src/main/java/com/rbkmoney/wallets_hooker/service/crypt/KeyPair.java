package com.rbkmoney.wallets_hooker.service.crypt;

public class KeyPair {
    private String privKey;
    private String publKey;

    public KeyPair(String privKey, String publKey) {
        this.privKey = privKey;
        this.publKey = publKey;
    }

    public String getPrivKey() {
        return privKey;
    }

    public String getPublKey() {
        return publKey;
    }
}
