package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import com.rbkmoney.wallets_hooker.utils.BuildUtils;
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
public class WithdrawalMessageDaoImplTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(WithdrawalMessageDaoImplTest.class);

    @Autowired
    WithdrawalMessageDao messageDao;

    private static boolean messagesCreated = false;

    @Before
    public void setUp() throws Exception {
        if(!messagesCreated){
            messageDao.create(BuildUtils.buildWithdrawalMessage(EventType.WITHDRAWAL_CREATED, "partyId", "withdrawalId1"));
            messageDao.create(BuildUtils.buildWithdrawalMessage(EventType.WITHDRAWAL_CREATED, "partyId", "withdrawalId2"));
            messageDao.create(BuildUtils.buildWithdrawalMessage(EventType.WITHDRAWAL_CREATED, "partyId", "withdrawalId3"));
            messagesCreated = true;
        }
    }

    @Test
    public void testGetAny() {
        WithdrawalMessage any = messageDao.getAny("withdrawalId1");
        assertEquals(any.getPartyId(), "partyId");
    }

    @Test
    public void get() throws Exception {
        WithdrawalMessage message = messageDao.getAny("walletId1");
        assertEquals(message.getPartyId(), "partyId");
        assertEquals(message.getEventType(), EventType.WITHDRAWAL_CREATED);
        assertEquals(1, messageDao.getBy(Arrays.asList(message.getId())).size());
    }

/*    @Test
    public void getMaxEventId() {
        assertEquals(messageDao.getLastEventId().longValue(), 123);
    }*/
}
