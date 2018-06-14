package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by inalarsanukaev on 09.04.17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WalletsMessageDaoImplTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(WalletsMessageDaoImplTest.class);

    @Autowired
    WalletsMessageDao messageDao;

    private static boolean messagesCreated = false;

    @Before
    public void setUp() throws Exception {
        if(!messagesCreated){
            messageDao.create(ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_CREATED", "partyId", "walletId1", "2016-03-22T06:12:27Z", 121));
            messageDao.create(ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_CREATED", "partyId", "walletId2", "2016-03-22T06:12:27Z", 122));
            messageDao.create(ConverterUtils.buildWalletsMessage("WALLET_WITHDRAWAL_CREATED", "partyId", "walletId3", "2016-03-22T06:12:27Z", 123));
            messagesCreated = true;
        }
    }

    @Test
    public void testGetAny() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            messageDao.getAny("walletId1");
        }

        long executionTime = System.currentTimeMillis() - startTime;
        if (executionTime > 1000) {
            log.error("Execution time: " + executionTime + ".Seems caching not working!!!");
        } else {
            log.info("Execution time: " + executionTime);
        }


    }

    @Test
    public void get() throws Exception {
        WalletsMessage message = messageDao.getAny("walletId1");
        assertEquals(message.getPartyId(), "partyId");
        assertEquals(message.getEventType(), EventType.WALLET_WITHDRAWAL_CREATED);
        assertEquals(1, messageDao.getBy(Arrays.asList(message.getId())).size());
    }

    @Test
    public void getMaxEventId() {
        assertEquals(messageDao.getMaxEventId().longValue(), 123);
    }
}
