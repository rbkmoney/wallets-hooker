package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.Message;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
import static org.junit.Assert.*;

public class WithdrawalMessageDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    private WithdrawalMessageDao withdrawalMessageDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<WithdrawalMessage> withdrawalMessageList;

    @Before
    public void before() {
        withdrawalMessageList = randomListOf(3, WithdrawalMessage.class);
        withdrawalMessageList.forEach(m -> m.setId(withdrawalMessageDao.create(m)));
    }

    @After
    public void after() {
        jdbcTemplate.execute("truncate whook.withdrawal_message");
    }

    @Test
    public void getAny() {
        WithdrawalMessage any = withdrawalMessageDao.getAny(withdrawalMessageList.get(0).getWithdrawalId());
        assertNotNull(any);
        assertEquals(any.getWithdrawalDestinationId(), withdrawalMessageList.get(0).getWithdrawalDestinationId());
    }

    @Test
    public void getLastEventId() {
        assertEquals(withdrawalMessageDao.getLastEventId().longValue(), withdrawalMessageList.stream().mapToLong(Message::getEventId).max().getAsLong());
    }

    @Test
    public void getBy() {
        List<Long> ids = withdrawalMessageList.stream().mapToLong(m -> m.getId()).boxed().collect(Collectors.toList());
        List<WithdrawalMessage> messagesFromDb = withdrawalMessageDao.getBy(ids);
        assertEquals(ids.size(), messagesFromDb.size());
        assertEquals(messagesFromDb.get(0).getWithdrawalId(), withdrawalMessageList.stream().filter(m -> m.getId().equals(messagesFromDb.get(0).getId())).findFirst().get().getWithdrawalId());
    }
}