package com.rbkmoney.wallets_hooker.dao.destination;

import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@ContextConfiguration(classes = {DestinationReferenceDaoImpl.class})
public class DestinationReferenceDaoImplTest extends AbstractPostgresIntegrationTest {

    @Autowired
    DestinationReferenceDao destinationReferenceDao;

    static {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas("whook")
                .load();
        flyway.migrate();
    }

    @Test
    public void create() {
        DestinationIdentityReference reference = new DestinationIdentityReference();
        reference.setIdentityId("identity");
        reference.setEventId("eventId");
        String destination = "destination";
        reference.setDestinationId(destination);
        reference.setExternalId("externalId");
        destinationReferenceDao.create(reference);

        DestinationIdentityReference destinationIdentityReference = destinationReferenceDao.get(destination);

        Assert.assertEquals(reference, destinationIdentityReference);
    }
}
