package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.constant.EventTopic;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@ContextConfiguration(classes = {EventLogDaoImpl.class})
public class EventLogDaoImplTest extends AbstractPostgresIntegrationTest {

    @Autowired
    EventLogDao eventLogDao;

    static {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas("whook")
                .load();
        flyway.migrate();
    }

    @Test
    public void create() {
        eventLogDao.create(1L, EventTopic.DESTINATION);
        eventLogDao.create(2L, EventTopic.DESTINATION);
        eventLogDao.create(5L, EventTopic.DESTINATION);

        Long lastEventId = eventLogDao.getLastEventId(EventTopic.DESTINATION, 0L);

        Assert.assertEquals(5L, lastEventId.longValue());
    }

    @Test
    public void getLastEventId() {
    }
}