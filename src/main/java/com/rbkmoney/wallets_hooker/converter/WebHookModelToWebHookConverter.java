package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.fistful.webhooker.Webhook;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.utils.WebHookConverterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebHookModelToWebHookConverter implements Converter<WebHookModel, Webhook> {

    @Override
    public Webhook convert(WebHookModel event) {
        Webhook webHook = new Webhook();
        webHook.setId(event.getId());
        webHook.setEnabled(event.getEnabled());
        webHook.setIdentityId(event.getIdentityId());
        webHook.setWalletId(event.getWalletId());
        webHook.setPubKey(event.getPubKey());
        webHook.setEventFilter(WebHookConverterUtils.generateEventFilter(event.getEventTypes()));
        webHook.setUrl(event.getUrl());
        return webHook;
    }

}