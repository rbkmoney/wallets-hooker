package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalQueueDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalTaskDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.TaskQueuePair;
import com.rbkmoney.wallets_hooker.model.WithdrawalQueue;
import com.rbkmoney.wallets_hooker.utils.BuildUtils;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by jeckep on 17.04.17.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WithdrawalTaskDaoTest extends AbstractIntegrationTest {
    @Autowired
    WithdrawalTaskDao taskDao;

    @Autowired
    WithdrawalQueueDao queueDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    WithdrawalMessageDao messageDao;

    Long messageId;
    Long hookId;

    @Before
    public void setUp() throws Exception {
        hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
        messageDao.create(BuildUtils.buildWithdrawalMessage(EventType.WITHDRAWAL_SUCCEEDED, "partyId", "withdrawalId"));
        messageId = messageDao.getAny("withdrawalId").getId();
    }

    @After
    public void after() throws Exception {
        hookDao.delete(hookId);
    }

    @Test
    public void createDeleteGet() {
        Map<Long, List<TaskQueuePair<WithdrawalQueue>>> scheduled = queueDao.getTaskQueuePairsMap(new ArrayList<>());
        assertEquals(1, scheduled.size());
        taskDao.remove(scheduled.keySet().iterator().next(), messageId);
        assertEquals(0, queueDao.getTaskQueuePairsMap(new ArrayList<>()).size());

    }

    @Test
    public void removeAll() {
        taskDao.removeAll(hookId);
    }
}
