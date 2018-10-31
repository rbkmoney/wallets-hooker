package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.fistful.webhooker.EventFilter;
import com.rbkmoney.fistful.webhooker.WebhookParams;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.model.MessageType;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HookDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    private HookDao hookDao;

    private List<Long> ids = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Set<Hook.WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL));
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL));
        EventFilter eventFilterByCode = ConverterUtils.convertEventFilter(webhookAdditionalFilters);
        WebhookParams webhookParams = new WebhookParams("123", eventFilterByCode, "https://google.com");
        Hook hook = hookDao.create(ConverterUtils.convertHook(webhookParams));
        String pubKey1 = hook.getPubKey();
        ids.add(hook.getId());
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL));
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL));
        webhookParams = new WebhookParams("999", ConverterUtils.convertEventFilter(webhookAdditionalFilters), "https://yandex.ru");
        hook = hookDao.create(ConverterUtils.convertHook(webhookParams));
        ids.add(hook.getId());
        webhookAdditionalFilters.clear();
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL));
        webhookParams = new WebhookParams("123", ConverterUtils.convertEventFilter(webhookAdditionalFilters), "https://2ch.hk/b");
        hook = hookDao.create(ConverterUtils.convertHook(webhookParams));
        String pubKey2 = hook.getPubKey();
        ids.add(hook.getId());
        Assert.assertEquals(pubKey1, pubKey2);
    }

    @After
    public void tearDown() throws Exception {
        List<Hook> list = hookDao.getPartyHooks("123");
        for (Hook w : list) {
            hookDao.delete(w.getId());
        }
        list = hookDao.getPartyHooks("999");
        for (Hook w : list) {
            hookDao.delete(w.getId());
        }
    }

    @Test
    public void testConstraint(){
        Set<Hook.WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL));
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL));
        WebhookParams webhookParams  = new WebhookParams("123", ConverterUtils.convertEventFilter(webhookAdditionalFilters), "https://2ch.hk/b");
        Hook hook = hookDao.create(ConverterUtils.convertHook(webhookParams));
        ids.add(hook.getId());
    }

    @Test
    public void getPartyWebhooks() throws Exception {
        assertEquals(hookDao.getPartyHooks("123").size(), 2);
        Assert.assertTrue(hookDao.getPartyHooks("88888").isEmpty());
    }

    @Test
    public void getWebhookById() throws Exception {
        List<Hook> list = hookDao.getPartyHooks("123");
        for (Hook w : list) {
            Assert.assertNotNull(hookDao.getHookById(w.getId()));
        }
    }

    public static Hook buildHook(String partyId, String url){
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setUrl(url);

        Set<Hook.WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL));
        webhookAdditionalFilters.add(new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL));
        hook.setFilters(webhookAdditionalFilters);

        return hook;
    }
}
