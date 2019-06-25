package com.rbkmoney.wallets_hooker.dao.wallet;

import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@ContextConfiguration(classes = {WalletReferenceDaoImpl.class})
public class WalletReferenceDaoImplTest extends AbstractPostgresIntegrationTest {

    @Autowired
    WalletReferenceDao walletReferenceDao;

    static {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas("whook")
                .load();
        flyway.migrate();
    }

    @Test
    public void create() {
        WalletIdentityReference reference = new WalletIdentityReference();
        reference.setIdentityId("identity");
        String walletId = "walletId";
        reference.setWalletId(walletId);
        walletReferenceDao.create(reference);

        WalletIdentityReference walletIdentityReference = walletReferenceDao.get(walletId);

        Assert.assertEquals(reference, walletIdentityReference);
    }
}