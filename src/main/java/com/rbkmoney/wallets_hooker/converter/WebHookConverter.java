package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.wallets_hooker.dao.identity.IdentityKeyDao;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookToEventsDao;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.IdentityKey;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WebhookToEvents;
import com.rbkmoney.wallets_hooker.utils.WebHookConverterUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WebHookConverter implements Converter<Webhook, com.rbkmoney.fistful.webhooker.Webhook> {

    private final IdentityKeyDao identityKeyDao;
    private final WebHookToEventsDao webHookToEventsDao;

    @Override
    public com.rbkmoney.fistful.webhooker.Webhook convert(Webhook webhook) {
        IdentityKey identityKey = identityKeyDao.getByIdentity(webhook.getIdentityId());
        Set<EventType> eventTypes = webHookToEventsDao.get(webhook.getId()).stream()
                .map(WebhookToEvents::getEventType)
                .collect(Collectors.toSet());

        var webHookDamsel = new com.rbkmoney.fistful.webhooker.Webhook();
        webHookDamsel.setId(webhook.getId());
        webHookDamsel.setEnabled(webhook.getEnabled());
        webHookDamsel.setIdentityId(webhook.getIdentityId());
        webHookDamsel.setWalletId(webhook.getWalletId());
        webHookDamsel.setPubKey(identityKey.getPubKey());
        webHookDamsel.setUrl(webhook.getUrl());
        webHookDamsel.setEventFilter(WebHookConverterUtils.generateEventFilter(eventTypes));
        return webHookDamsel;
    }
}
