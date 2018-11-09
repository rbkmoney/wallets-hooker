package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.model.Message;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
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

public class WalletMessageDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    private WalletMessageDao walletMessageDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<WalletMessage> walletMessageList;

    @Before
    public void before() {
        walletMessageList = randomListOf(3, WalletMessage.class);
        walletMessageList.forEach(m -> m.setId(walletMessageDao.create(m)));
    }

    @After
    public void after() {
        jdbcTemplate.execute("truncate whook.wallet_message");
    }

    @Test
    public void getAny() {
        WalletMessage any = walletMessageDao.getAny(walletMessageList.get(0).getWalletId());
        assertNotNull(any);
        assertEquals(any.getOccuredAt(), walletMessageList.get(0).getOccuredAt());
    }

    @Test
    public void getLastEventId() {
        assertEquals(walletMessageDao.getLastEventId().longValue(), walletMessageList.stream().mapToLong(Message::getEventId).max().getAsLong());
    }

    @Test
    public void getBy() {
        List<Long> ids = walletMessageList.stream().mapToLong(m -> m.getId()).boxed().collect(Collectors.toList());
        List<WalletMessage> messagesFromDb = walletMessageDao.getBy(ids);
        assertEquals(ids.size(), messagesFromDb.size());
        assertEquals(messagesFromDb.get(0).getWalletId(), walletMessageList.stream().filter(m -> m.getId().equals(messagesFromDb.get(0).getId())).findFirst().get().getWalletId());
    }
}