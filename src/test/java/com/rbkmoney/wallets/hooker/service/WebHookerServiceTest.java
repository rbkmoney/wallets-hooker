package com.rbkmoney.wallets.hooker.service;

import com.rbkmoney.fistful.webhooker.Webhook;
import com.rbkmoney.wallets.hooker.converter.WebHookConverter;
import com.rbkmoney.wallets.hooker.converter.WebHookModelToWebHookConverter;
import com.rbkmoney.wallets.hooker.converter.WebHookParamsToWebHookConverter;
import com.rbkmoney.wallets.hooker.dao.webhook.WebHookDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class WebHookerServiceTest {

    @Mock
    private WebHookDao webHookDao;
    @Mock
    private WebHookConverter webHookConverter;
    @Mock
    private WebHookParamsToWebHookConverter webHookParamsToWebHookConverter;
    @Mock
    private WebHookModelToWebHookConverter webHookModelToWebHookConverter;
    private WebHookerService webHookerService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        webHookerService = new WebHookerService(webHookDao, webHookConverter, webHookParamsToWebHookConverter,
                webHookModelToWebHookConverter);
    }

    @Test
    public void getList() {
        String id = "test";
        ArrayList<com.rbkmoney.wallets.hooker.domain.tables.pojos.Webhook> webhooks = new ArrayList<>();
        com.rbkmoney.wallets.hooker.domain.tables.pojos.Webhook webhook =
                new com.rbkmoney.wallets.hooker.domain.tables.pojos.Webhook();
        webhook.setIdentityId(id);
        webhooks.add(webhook);
        Mockito.when(webHookDao.getByIdentity(id)).thenReturn(webhooks);
        Webhook hook = new Webhook();
        hook.setIdentityId(id);
        Mockito.when(webHookConverter.convert(webhook)).thenReturn(hook);
        List<Webhook> listWebHooks = webHookerService.getList(id);

        Assert.assertFalse(listWebHooks.isEmpty());
        Assert.assertEquals(id, listWebHooks.get(0).identity_id);
    }

}