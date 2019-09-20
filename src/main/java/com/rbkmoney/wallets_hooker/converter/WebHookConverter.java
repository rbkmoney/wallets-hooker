package com.rbkmoney.wallets_hooker.converter;

import com.rbkmoney.fistful.webhooker.EventFilter;
import com.rbkmoney.wallets_hooker.dao.identity.IdentityKeyDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.IdentityKey;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class WebHookConverter implements Converter<Webhook, com.rbkmoney.fistful.webhooker.Webhook> {

    private final IdentityKeyDao identityKeyDao;

    @Override
    public com.rbkmoney.fistful.webhooker.Webhook convert(Webhook event) {
        com.rbkmoney.fistful.webhooker.Webhook webHook = new com.rbkmoney.fistful.webhooker.Webhook();
        webHook.setId(event.getId());
        webHook.setEnabled(event.getEnabled());
        webHook.setIdentityId(event.getIdentityId());
        webHook.setWalletId(event.getWalletId());
        IdentityKey identityKey = identityKeyDao.getByIdentity(event.getIdentityId());
        webHook.setPubKey(identityKey.getPubKey());
        webHook.setUrl(event.getUrl());
        return webHook;
    }
}
