package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.wallets_hooker.logging.HttpLoggingInterceptor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class PostSender {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String SIGNATURE_HEADER = "Content-Signature";
    public static final long RESPONSE_MAX_LENGTH = 4096L;

    public PostSender(@Value("${merchant.callback.timeout}") int timeout) {
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();

        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(message -> log.debug(message));
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpBuilder.addInterceptor(httpLoggingInterceptor);
        }

        this.httpClient = httpBuilder
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public int doPost(String url, long messageId, String paramsAsString, String signature) throws IOException {
        log.info("Sending message with id {}, {} to hook: {} ", messageId, paramsAsString, url);
        RequestBody body = RequestBody.create(JSON, paramsAsString);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader(SIGNATURE_HEADER, "alg=RS256; digest=" + signature)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            log.info("Response from hook: messageId: {}, code: {}; body: {}", messageId, response.code(), response.body() != null ? response.peekBody(RESPONSE_MAX_LENGTH).string() : "<empty>");
            return response.code();
        }
    }
}
