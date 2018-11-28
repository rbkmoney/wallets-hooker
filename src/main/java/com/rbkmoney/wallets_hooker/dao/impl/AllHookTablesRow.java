package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.model.Hook;

/**
 * Created by inalarsanukaev on 18.04.17.
 */
public class AllHookTablesRow {
    private long id;
    private String partyId;
    private String url;
    private String pubKey;
    private boolean enabled;
    private Hook.WebhookAdditionalFilter webhookAdditionalFilter;

    public AllHookTablesRow(long id, String partyId, String url, String pubKey, boolean enabled, Hook.WebhookAdditionalFilter webhookAdditionalFilter) {
        this.id = id;
        this.partyId = partyId;
        this.url = url;
        this.pubKey = pubKey;
        this.enabled = enabled;
        this.webhookAdditionalFilter = webhookAdditionalFilter;
    }

    public Hook.WebhookAdditionalFilter getWebhookAdditionalFilter() {
        return webhookAdditionalFilter;
    }

    public void setWebhookAdditionalFilter(Hook.WebhookAdditionalFilter webhookAdditionalFilter) {
        this.webhookAdditionalFilter = webhookAdditionalFilter;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
