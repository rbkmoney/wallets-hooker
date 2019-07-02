package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.fistful.webhooker.WebhookParams;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.utils.EventTypeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebHookParamsToWebHookConverter implements Converter<WebhookParams, WebHookModel> {

    @Override
    public WebHookModel convert(WebhookParams event) {
        return WebHookModel.builder()
                .identityId(event.getIdentityId())
                .url(event.getUrl())
                .walletId(event.getWalletId())
                .eventTypes(EventTypeUtils.convertEventTypes(event))
                .build();

    }


}