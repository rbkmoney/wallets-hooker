package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.HookDao;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.*;

public class WithdrawalTaskDaoTest extends AbstractIntegrationTest {

    @Autowired
    private WithdrawalQueueDao withdrawalQueueDao;

    @Autowired
    private WithdrawalTaskDao withdrawalTaskDao;

    @Autowired
    private WithdrawalMessageDao withdrawalMessageDao;

    @Autowired
    private HookDao hookDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Hook hook1;

    private static final String partyId1 = "partyId1";

    @Before
    public void before() {
        hook1 = random(Hook.class);
        hook1.setPartyId(partyId1);
        hook1.setFilters(new HashSet<>(Arrays.asList(
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL),
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL))));
        hook1 = hookDao.create(hook1);
    }

    @After
    public void after() {
        hookDao.delete(hook1.getId());
        jdbcTemplate.execute("truncate whook.withdrawal_message");
    }

    @Test
    public void create() {
        WithdrawalMessage message = random(WithdrawalMessage.class);
        message.setPartyId(partyId1);
        message.setEventType(EventType.WITHDRAWAL_SUCCEEDED);
        Long message2Id = withdrawalMessageDao.create(message);
        withdrawalQueueDao.createByMessageId(message2Id);
        int count = withdrawalTaskDao.createByMessageId(message2Id);
        assertEquals(count, 1);
    }
}