package com.rbkmoney.wallets_hooker.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.concurrent.TimeUnit;

@Service
public class PostSender {
    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String SIGNATURE_HEADER = "Content-Signature";

    public PostSender(@Value("${merchant.callback.timeout}") int timeout) {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public AbstractMap.SimpleEntry<Integer, String> doPost(String url, String paramsAsString, String signature) throws IOException {
        RequestBody body = RequestBody.create(JSON, paramsAsString);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader(SIGNATURE_HEADER, "alg=RS256; digest=" + signature)
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        return new AbstractMap.SimpleEntry<>(response.code(), response.body().string());
    }
}
