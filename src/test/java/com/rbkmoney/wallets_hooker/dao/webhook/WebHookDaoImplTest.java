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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;

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

        assertEquals(IDENTITY_ID, webHookModel.getIdentityId());
        assertEquals(WALLET_123, webHookModel.getWalletId());
        assertEquals(eventTypes, webHookModel.getEventTypes());
        assertFalse(webHookModel.getPubKey().isEmpty());
        assertNotNull(webHookModel.getEventTypes());

        webHookDao.delete(webhook1.getId());

        webHookModel = webHookDao.getById(webhook1.getId());

        assertNull(webHookModel);

        eventTypes = new LinkedHashSet<>();
        eventTypes.add(EventType.DESTINATION_CREATED);
        eventTypes.add(EventType.DESTINATION_AUTHORIZED);
        webhook = WebHookModel.builder()
                .enabled(true)
                .identityId(IDENTITY_ID)
                .url("/qwe")
                .walletId(null)
                .eventTypes(eventTypes)
                .build();
        Webhook webhook2 = webHookDao.create(webhook);

        List<WebHookModel> modelByIdentityAndWalletId = webHookDao.getModelByIdentityAndWalletId(IDENTITY_ID, null, EventType.DESTINATION_CREATED);

        assertEquals(1, modelByIdentityAndWalletId.size());
        assertNotNull(modelByIdentityAndWalletId.get(0).getEventTypes());

        webHookDao.create(webhook);
        modelByIdentityAndWalletId = webHookDao.getModelByIdentityAndWalletId(IDENTITY_ID, null, EventType.DESTINATION_CREATED);
        assertEquals(2, modelByIdentityAndWalletId.size());
        assertNotNull(modelByIdentityAndWalletId.get(0).getEventTypes());

        modelByIdentityAndWalletId = webHookDao.getModelByIdentityAndWalletId(IDENTITY_ID, WALLET_123, EventType.DESTINATION_CREATED);

        assertEquals(0, modelByIdentityAndWalletId.size());
    }
}