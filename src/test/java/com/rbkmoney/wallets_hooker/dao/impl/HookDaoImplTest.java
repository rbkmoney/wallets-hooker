package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.HookDao;
import com.rbkmoney.wallets_hooker.model.Hook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
import static org.junit.Assert.*;

public class HookDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    private HookDao hookDao;

    private List<Hook> hooks;

    @Before
    public void before(){
        hooks = randomListOf(3, Hook.class);
        List<Hook> hooksFormDb = hooks.stream().map(h -> hookDao.create(h)).collect(Collectors.toList());
        assertEquals(hooksFormDb.size(), hooks.size());
    }

    @After
    public void after(){
        hooks.forEach(h -> hookDao.delete(h.getId()));
    }

    @Test
    public void getPartyHooks() {
        assertNotNull(hookDao.getPartyHooks(hooks.get(0).getPartyId()));
    }

    @Test
    public void getHookById() {
        assertNotNull(hookDao.getHookById(hooks.get(0).getId()));
    }

    @Test
    public void delete() {
        hookDao.delete(hooks.get(0).getId());
        assertTrue(hookDao.getPartyHooks(hooks.get(0).getPartyId()).isEmpty());
    }
}