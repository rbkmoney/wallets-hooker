package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.impl.WalletsQueueDao;
import com.rbkmoney.wallets_hooker.dao.impl.WalletsTaskDao;
import com.rbkmoney.wallets_hooker.model.TaskQueuePair;
import com.rbkmoney.wallets_hooker.model.WalletsQueue;
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
public class WalletsTaskDaoTest extends AbstractIntegrationTest {
    @Autowired
    WalletsTaskDao taskDao;

    @Autowired
    WalletsQueueDao queueDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    WalletsMessageDao messageDao;

    Long messageId;
    Long hookId;

    @Before
    public void setUp() throws Exception {
        hookId = hookDao.create(HookDaoImplTest.buildHook("partyId", "fake.url")).getId();
        messageDao.create(ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_SUCCEEDED", "partyId", "walletId", "2016-03-22T06:12:27Z", 123));
        messageId = messageDao.getAny("walletId").getId();
    }

    @After
    public void after() throws Exception {
        hookDao.delete(hookId);
    }

    @Test
    public void createDeleteGet() {
        Map<Long, List<TaskQueuePair<WalletsQueue>>> scheduled = queueDao.getTaskQueuePairsMap(new ArrayList<>());
        assertEquals(1, scheduled.size());
        taskDao.remove(scheduled.keySet().iterator().next(), messageId);
        assertEquals(0, queueDao.getTaskQueuePairsMap(new ArrayList<>()).size());

    }

    @Test
    public void removeAll() {
        taskDao.removeAll(hookId);
    }
}
