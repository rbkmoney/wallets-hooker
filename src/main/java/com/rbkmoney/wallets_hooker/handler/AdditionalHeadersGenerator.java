package com.rbkmoney.wallets_hooker.handler;

import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdditionalHeadersGenerator {

    private final Signer signer;

    public static final String SIGNATURE_HEADER = "Content-Signature";

    public Map<String, String> generate(WebHookModel model, String requestBody) {
        String signature = signer.sign(requestBody, model.getPrivateKey());
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(SIGNATURE_HEADER, "alg=RS256; digest=" + signature);
        return additionalHeaders;
    }

}
