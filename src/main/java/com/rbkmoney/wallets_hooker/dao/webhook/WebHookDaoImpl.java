package com.rbkmoney.wallets_hooker.dao.webhook;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets_hooker.dao.AbstractDao;
import com.rbkmoney.wallets_hooker.dao.identity.IdentityKeyDao;
import com.rbkmoney.wallets_hooker.dao.condition.ConditionParameterSource;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.IdentityKey;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WebhookToEvents;
import com.rbkmoney.wallets_hooker.domain.tables.records.WebhookRecord;
import com.rbkmoney.wallets_hooker.domain.tables.records.WebhookToEventsRecord;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rbkmoney.wallets_hooker.domain.tables.IdentityKey.IDENTITY_KEY;
import static com.rbkmoney.wallets_hooker.domain.tables.Webhook.WEBHOOK;
import static com.rbkmoney.wallets_hooker.domain.tables.WebhookToEvents.WEBHOOK_TO_EVENTS;
import static org.jooq.Comparator.EQUALS;

@Component
public class WebHookDaoImpl extends AbstractDao implements WebHookDao {

    private static final int LIMIT = 1000;
    private final RowMapper<com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook> listRecordRowMapper;
    private final RowMapper<WebHookModel> webHookModelRowMapper;
    private final WebHookToEventsDao webHookToEventsDao;
    private final IdentityKeyDao identityKeyDao;
    private final Signer signer;

    public WebHookDaoImpl(DataSource dataSource, WebHookToEventsDao webHookToEventsDao, IdentityKeyDao identityKeyDao,
                          Signer signer) {
        super(dataSource);
        this.webHookToEventsDao = webHookToEventsDao;
        this.identityKeyDao = identityKeyDao;
        this.signer = signer;
        this.listRecordRowMapper = new RecordRowMapper<>(WEBHOOK, com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook.class);
        this.webHookModelRowMapper = new RowMapper<WebHookModel>() {

            @Override
            public WebHookModel mapRow(ResultSet rs, int i) throws SQLException {
                return WebHookModel.builder()
                        .id(rs.getLong(WEBHOOK.ID.getName()))
                        .enabled(rs.getBoolean(WEBHOOK.ENABLED.getName()))
                        .identityId(rs.getString(WEBHOOK.IDENTITY_ID.getName()))
                        .walletId(rs.getString(WEBHOOK.WALLET_ID.getName()))
                        .url(rs.getString(WEBHOOK.URL.getName()))
                        .pubKey(rs.getString(IDENTITY_KEY.PUB_KEY.getName()))
                        .build();
            }
        };
    }

    @Override
    public Webhook create(WebHookModel webhook) {
        String identityId = webhook.getIdentityId();
        IdentityKey identityKey = identityKeyDao.getByIdentity(identityId);

        if (identityKey == null) {
            identityKey = new IdentityKey();
            identityKey.setIdentityId(identityId);
            KeyPair keyPair = signer.generateKeys();
            String publKey = keyPair.getPublKey();
            identityKey.setPubKey(publKey);
            identityKey.setPrivKey(keyPair.getPrivKey());
            identityKeyDao.create(identityKey);
        }

        InsertResultStep<WebhookRecord> returning = getDslContext()
                .insertInto(WEBHOOK)
                .set(getDslContext()
                        .newRecord(WEBHOOK, webhook))
                .onConflict(WEBHOOK.ID)
                .doNothing()
                .returning(WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID);
        Webhook webhookResult = fetchOne(returning, listRecordRowMapper);

        webhook.getEventTypes()
                .forEach(eventType -> webHookToEventsDao.create(new WebhookToEvents(webhookResult.getId(), eventType)));

        return webhookResult;
    }

    @Override
    public void delete(long id) {
        DeleteConditionStep<WebhookToEventsRecord> hookToEvents = getDslContext()
                .delete(WEBHOOK_TO_EVENTS)
                .where(WEBHOOK_TO_EVENTS.HOOK_ID.eq(id));
        execute(hookToEvents);
        DeleteConditionStep<com.rbkmoney.wallets_hooker.domain.tables.records.WebhookRecord> webhook = getDslContext()
                .delete(WEBHOOK)
                .where(WEBHOOK.ID.eq(id));
        execute(webhook);
    }

    @Override
    public WebHookModel getById(long id) {
        SelectConditionStep<Record6<Long, String, Boolean, String, String, String>> where = getDslContext()
                .select(WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID,
                        IDENTITY_KEY.PUB_KEY)
                .from(WEBHOOK)
                .leftJoin(IDENTITY_KEY).on(WEBHOOK.IDENTITY_ID.eq(IDENTITY_KEY.IDENTITY_ID))
                .where(WEBHOOK.ID.eq(id));

        WebHookModel webHookModel = fetchOne(where, webHookModelRowMapper);

        if (webHookModel != null) {
            Set<EventType> collect = webHookToEventsDao.get(id).stream()
                    .map(WebhookToEvents::getEventType)
                    .collect(Collectors.toSet());
            webHookModel.setEventTypes(collect);
        }
        return webHookModel;
    }

    @Override
    public List<Webhook> getByIdentityAndWalletId(String identityId, String walletId, EventType eventType) {
        Condition condition = DSL.trueCondition();
        SelectLimitPercentStep<Record5<Long, String, Boolean, String, String>> query =
                getDslContext()
                        .select(WEBHOOK.ID,
                                WEBHOOK.IDENTITY_ID,
                                WEBHOOK.ENABLED,
                                WEBHOOK.URL,
                                WEBHOOK.WALLET_ID)
                        .from(WEBHOOK)
                        .join(WEBHOOK_TO_EVENTS).on(WEBHOOK.ID.eq(WEBHOOK_TO_EVENTS.HOOK_ID))
                        .where(
                                appendConditions(condition, Operator.OR,
                                        new ConditionParameterSource()
                                                .addValue(WEBHOOK.IDENTITY_ID, identityId, EQUALS)
                                                .addValue(WEBHOOK.WALLET_ID, walletId, EQUALS)
                                ))
                        .and(WEBHOOK_TO_EVENTS.EVENT_TYPE.eq(eventType))
                        .limit(LIMIT);
        return fetch(query, listRecordRowMapper);
    }

    @Override
    public List<WebHookModel> getModelByIdentityAndWalletId(String identityId, String walletId, EventType eventType) {
        Condition condition = DSL.trueCondition();
        SelectLimitPercentStep<Record7<Long, String, Boolean, String, String, EventType, String>> record = getDslContext()
                .select(WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID,
                        WEBHOOK_TO_EVENTS.EVENT_TYPE,
                        IDENTITY_KEY.PUB_KEY)
                .from(WEBHOOK)
                .join(WEBHOOK_TO_EVENTS).on(WEBHOOK.ID.eq(WEBHOOK_TO_EVENTS.HOOK_ID))
                .join(IDENTITY_KEY).on(WEBHOOK.IDENTITY_ID.eq(IDENTITY_KEY.IDENTITY_ID))
                .where(
                        appendConditions(condition, Operator.OR,
                                new ConditionParameterSource()
                                        .addValue(WEBHOOK.IDENTITY_ID, identityId, EQUALS)
                                        .addValue(WEBHOOK.WALLET_ID, walletId, EQUALS)
                        ))
                .and(WEBHOOK_TO_EVENTS.EVENT_TYPE.eq(eventType))
                .limit(LIMIT);
        return fetch(record, webHookModelRowMapper);
    }

    @Override
    public List<Webhook> getByIdentity(String identityId) {
        SelectLimitPercentStep<Record5<Long, String, Boolean, String, String>> record = getDslContext()
                .select(WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID)
                .from(WEBHOOK)
                .where(WEBHOOK.IDENTITY_ID.eq(identityId))
                .limit(LIMIT);
        return fetch(record, listRecordRowMapper);
    }
}
