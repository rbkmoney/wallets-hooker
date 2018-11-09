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

import java.time.Instant;
import java.util.*;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.junit.Assert.*;

public class WithdrawalQueueDaoTest extends AbstractIntegrationTest {

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
    private Hook hook2;
    private Hook hook3;

    private static final String partyId1 = "partyId1";
    private static final String partyId2 = "partyId2";

    @Before
    public void before() {
        hook1 = random(Hook.class);
        hook1.setPartyId(partyId1);
        hook1.setFilters(new HashSet<>(Arrays.asList(
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_CREATED, MessageType.WITHDRAWAL),
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL))));
        hook1 = hookDao.create(hook1);
        hook2 = random(Hook.class);
        hook2.setPartyId(partyId2);
        hook2.setFilters(new HashSet<>(Arrays.asList(
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL),
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_FAILED, MessageType.WITHDRAWAL))));
        hook2 = hookDao.create(hook2);
        hook3 = random(Hook.class);
        hook3.setPartyId(partyId1);
        hook3.setFilters(new HashSet<>(Arrays.asList(
                new Hook.WebhookAdditionalFilter(EventType.WITHDRAWAL_SUCCEEDED, MessageType.WITHDRAWAL))));
        hook3 = hookDao.create(hook3);
    }

    @After
    public void after() {
        hookDao.delete(hook1.getId());
        hookDao.delete(hook2.getId());
        hookDao.delete(hook3.getId());
        jdbcTemplate.execute("truncate whook.withdrawal_message");
    }

    @Test
    public void createByMessageId() {
        WithdrawalMessage message1 = random(WithdrawalMessage.class);
        message1.setPartyId(partyId1);
        Long message1Id = withdrawalMessageDao.create(message1);
        int count = withdrawalQueueDao.createByMessageId(message1Id);
        assertEquals(count, 2);

        WithdrawalMessage message2 = random(WithdrawalMessage.class);
        message2.setPartyId(partyId2);
        Long message2Id = withdrawalMessageDao.create(message2);
        count = withdrawalQueueDao.createByMessageId(message2Id);
        assertEquals(count, 1);

        WithdrawalMessage message3 = random(WithdrawalMessage.class);
        message3.setPartyId("kek");
        Long message3Id = withdrawalMessageDao.create(message3);
        count = withdrawalQueueDao.createByMessageId(message3Id);
        assertEquals(count, 0);
    }

    @Test
    public void getTaskQueuePairsMap() {
        WithdrawalMessage message1 = random(WithdrawalMessage.class);
        message1.setPartyId(partyId1);
        message1.setEventType(EventType.WITHDRAWAL_CREATED);
        Long message1Id = withdrawalMessageDao.create(message1);
        withdrawalQueueDao.createByMessageId(message1Id);
        withdrawalTaskDao.createByMessageId(message1Id);
        Map<Long, List<TaskQueuePair<WithdrawalQueue>>> taskQueuePairsMap = withdrawalQueueDao.getTaskQueuePairsMap(Collections.EMPTY_LIST);
        assertEquals(taskQueuePairsMap.entrySet().size(), 1);
        WithdrawalQueue queue = taskQueuePairsMap.values().stream().findFirst().get().get(0).getQueue();
        assertEquals(queue.getWithdrawalId(), message1.getWithdrawalId());
        assertEquals(queue.getFailCount(), 0);
        assertEquals(queue.getNextTime(), 0);
        assertEquals(queue.getLastFailTime(), 0);
        assertEquals(queue.getHook().getPartyId(), message1.getPartyId());
        taskQueuePairsMap.keySet().forEach(qId -> withdrawalTaskDao.removeAll(qId));

        WithdrawalMessage message2 = random(WithdrawalMessage.class);
        message2.setPartyId(partyId1);
        message2.setEventType(EventType.WITHDRAWAL_SUCCEEDED);
        Long message2Id = withdrawalMessageDao.create(message2);
        withdrawalQueueDao.createByMessageId(message2Id);
        withdrawalTaskDao.createByMessageId(message2Id);
        taskQueuePairsMap = withdrawalQueueDao.getTaskQueuePairsMap(Collections.EMPTY_LIST);
        assertEquals(taskQueuePairsMap.entrySet().size(), 2);
        taskQueuePairsMap.keySet().forEach(qId -> withdrawalTaskDao.removeAll(qId));

        WithdrawalMessage message3 = random(WithdrawalMessage.class);
        message3.setPartyId("kek");
        message3.setEventType(EventType.WITHDRAWAL_FAILED);
        Long message3Id = withdrawalMessageDao.create(message3);
        withdrawalQueueDao.createByMessageId(message3Id);
        withdrawalTaskDao.createByMessageId(message3Id);
        taskQueuePairsMap = withdrawalQueueDao.getTaskQueuePairsMap(Collections.EMPTY_LIST);
        assertEquals(taskQueuePairsMap.entrySet().size(), 0);
        taskQueuePairsMap.keySet().forEach(qId -> withdrawalTaskDao.removeAll(qId));
    }

    @Test
    public void updateRetries() {
        WithdrawalMessage message1 = random(WithdrawalMessage.class);
        message1.setPartyId(partyId1);
        message1.setEventType(EventType.WITHDRAWAL_CREATED);
        Long message1Id = withdrawalMessageDao.create(message1);
        withdrawalQueueDao.createByMessageId(message1Id);
        withdrawalTaskDao.createByMessageId(message1Id);
        Map<Long, List<TaskQueuePair<WithdrawalQueue>>> taskQueuePairsMap = withdrawalQueueDao.getTaskQueuePairsMap(Collections.EMPTY_LIST);
        assertEquals(taskQueuePairsMap.entrySet().size(), 1);
        WithdrawalQueue queue = taskQueuePairsMap.values().stream().findFirst().get().get(0).getQueue();
        queue.setFailCount(1);
        queue.setLastFailTime(Instant.now().getEpochSecond());
        queue.setNextTime(Instant.now().getEpochSecond());
        withdrawalQueueDao.updateRetries(queue);
    }

    @Test
    public void disable() {
        WithdrawalMessage message1 = random(WithdrawalMessage.class);
        message1.setPartyId(partyId1);
        message1.setEventType(EventType.WITHDRAWAL_CREATED);
        Long message1Id = withdrawalMessageDao.create(message1);
        withdrawalQueueDao.createByMessageId(message1Id);
        withdrawalTaskDao.createByMessageId(message1Id);
        Map<Long, List<TaskQueuePair<WithdrawalQueue>>> taskQueuePairsMap = withdrawalQueueDao.getTaskQueuePairsMap(Collections.EMPTY_LIST);
        assertEquals(taskQueuePairsMap.entrySet().size(), 1);
        WithdrawalQueue queue = taskQueuePairsMap.values().stream().findFirst().get().get(0).getQueue();
        withdrawalQueueDao.disable(queue.getId());
    }
}