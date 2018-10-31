package com.rbkmoney.wallets_hooker.scheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.wallets_hooker.model.Message;

public abstract class MessageConverter<M extends Message> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    protected static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public abstract String convertToJson(M message);
}
