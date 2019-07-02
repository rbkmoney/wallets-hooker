package com.rbkmoney.wallets_hooker.dao.webhook;

import com.rbkmoney.wallets_hooker.converter.WebHookModelToWebHookConverter;
import com.rbkmoney.wallets_hooker.dao.AbstractPostgresIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.identity.IdentityKeyDaoImpl;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;
import com.rbkmoney.wallets_hooker.service.crypt.AsymSigner;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.LinkedHashSet;

@Slf4j
@ContextConfiguration(classes = {WebHookDaoImpl.class, WebHookModelToWebHookConverter.class,
        WebHookToEventsDaoImpl.class, IdentityKeyDaoImpl.class, AsymSigner.class})
public class WebHookDaoImplTest extends AbstractPostgresIntegrationTest {

    public static final String IDENTITY_ID = "123";
    public static final String WALLET_123 = "wallet_123";

    @Autowired
    private WebHookDao webHookDao;

    static {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas("whook")
                .load();
        flyway.migrate();
    }

    @Test
    public void create() {
        LinkedHashSet<EventType> eventTypes = new LinkedHashSet<>();
        eventTypes.add(EventType.WITHDRAWAL_CREATED);
        eventTypes.add(EventType.WITHDRAWAL_SUCCEEDED);
        WebHookModel webhook = WebHookModel.builder()
                .enabled(true)
                .identityId(IDENTITY_ID)
                .url("/qwe")
                .walletId(WALLET_123)
                .eventTypes(eventTypes)
                .build();
        Webhook webhook1 = webHookDao.create(webhook);

        WebHookModel webHookModel = webHookDao.getById(webhook1.getId());

        Assert.assertEquals(IDENTITY_ID, webHookModel.getIdentityId());
        Assert.assertEquals(WALLET_123, webHookModel.getWalletId());
        Assert.assertEquals(eventTypes, webHookModel.getEventTypes());
        Assert.assertFalse(webHookModel.getPubKey().isEmpty());

        webHookDao.delete(webhook1.getId());

        webHookModel = webHookDao.getById(webhook1.getId());

        Assert.assertNull(webHookModel);
    }
}