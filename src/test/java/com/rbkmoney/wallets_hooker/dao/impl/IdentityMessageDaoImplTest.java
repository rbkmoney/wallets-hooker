package com.rbkmoney.wallets_hooker.dao.impl;

import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.IdentityMessageDao;
import com.rbkmoney.wallets_hooker.model.IdentityMessage;
import com.rbkmoney.wallets_hooker.model.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IdentityMessageDaoImplTest extends AbstractIntegrationTest {

    @Autowired
    private IdentityMessageDao identityMessageDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<IdentityMessage> identityMessageList;

    @Before
    public void before() {
        identityMessageList = randomListOf(3, IdentityMessage.class);
        identityMessageList.forEach(m -> m.setId(identityMessageDao.create(m)));
    }

    @After
    public void after() {
        jdbcTemplate.execute("truncate whook.identity_message");
    }

    @Test
    public void getAny() {
        IdentityMessage any = identityMessageDao.getAny(identityMessageList.get(0).getIdentityId());
        assertNotNull(any);
        assertEquals(any.getOccuredAt(), identityMessageList.get(0).getOccuredAt());
    }

    @Test
    public void getLastEventId() {
        assertEquals(identityMessageDao.getLastEventId().longValue(), identityMessageList.stream().mapToLong(Message::getEventId).max().getAsLong());
    }

    @Test
    public void getBy() {
        List<Long> ids = identityMessageList.stream().mapToLong(m -> m.getId()).boxed().collect(Collectors.toList());
        List<IdentityMessage> messagesFromDb = identityMessageDao.getBy(ids);
        assertEquals(ids.size(), messagesFromDb.size());
        assertEquals(messagesFromDb.get(0).getIdentityId(), identityMessageList.stream().filter(m -> m.getId().equals(messagesFromDb.get(0).getId())).findFirst().get().getIdentityId());
    }
}