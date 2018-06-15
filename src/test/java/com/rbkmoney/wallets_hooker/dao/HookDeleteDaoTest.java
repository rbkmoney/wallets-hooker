package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.impl.WalletsQueueDao;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static com.rbkmoney.wallets_hooker.dao.HookDaoImplTest.buildHook;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HookDeleteDaoTest extends AbstractIntegrationTest {


    @Autowired
    WalletsQueueDao queueDao;

    @Autowired
    HookDao hookDao;

    @Autowired
    WalletsMessageDao messageDao;

    @Test
    public void test() {
        Long hookId = hookDao.create(buildHook("partyId", "fake.url")).getId();
        Long hookId2 = hookDao.create(buildHook("partyId2", "fake2.url")).getId();
        messageDao.create(ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_SUCCEEDED", "partyId", "walletId1", "2016-03-22T06:12:27Z", 123));
        messageDao.create(ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_FAILED", "partyId2", "walletId2", "2016-03-22T06:12:27Z", 124));
        assertEquals(queueDao.getTaskQueuePairsMap(new ArrayList<>()).keySet().size(), 2);
        hookDao.delete(hookId2);
        assertEquals(queueDao.getTaskQueuePairsMap(new ArrayList<>()).keySet().size(), 1);
        hookDao.delete(hookId);
        assertEquals(queueDao.getTaskQueuePairsMap(new ArrayList<>()).keySet().size(), 0);
    }
}
