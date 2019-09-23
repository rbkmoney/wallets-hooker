package com.rbkmoney.wallets_hooker.dao.webhook;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets_hooker.dao.AbstractDao;
import com.rbkmoney.wallets_hooker.dao.condition.ConditionParameterSource;
import com.rbkmoney.wallets_hooker.dao.identity.IdentityKeyDao;
import com.rbkmoney.wallets_hooker.dao.webhook.mapper.WebHookModelRowMapper;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.IdentityKey;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WebhookToEvents;
import com.rbkmoney.wallets_hooker.domain.tables.records.WebhookRecord;
import com.rbkmoney.wallets_hooker.service.crypt.KeyPair;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Operator;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.rbkmoney.wallets_hooker.domain.tables.IdentityKey.IDENTITY_KEY;
import static com.rbkmoney.wallets_hooker.domain.tables.Webhook.WEBHOOK;
import static com.rbkmoney.wallets_hooker.domain.tables.WebhookToEvents.WEBHOOK_TO_EVENTS;
import static com.rbkmoney.wallets_hooker.utils.LogUtils.getLogWebHookModel;
import static org.jooq.Comparator.EQUALS;

@Component
@Slf4j
public class WebHookDaoImpl extends AbstractDao implements WebHookDao {

    private static final int LIMIT = 1000;
    private final RowMapper<Webhook> webhookRowMapper;
    private final RowMapper<WebHookModel> webHookModelRowMapper;
    private final WebHookToEventsDao webHookToEventsDao;
    private final IdentityKeyDao identityKeyDao;
    private final Signer signer;

    @Autowired
    public WebHookDaoImpl(HikariDataSource dataSource, WebHookToEventsDao webHookToEventsDao, IdentityKeyDao identityKeyDao,
                          Signer signer) {
        super(dataSource);
        this.webHookToEventsDao = webHookToEventsDao;
        this.identityKeyDao = identityKeyDao;
        this.signer = signer;
        this.webhookRowMapper = new RecordRowMapper<>(WEBHOOK, Webhook.class);
        this.webHookModelRowMapper = new WebHookModelRowMapper();
    }

    @Override
    public Webhook create(WebHookModel webHookModel) {
        String identityId = webHookModel.getIdentityId();

        IdentityKey identityKey = identityKeyDao.getByIdentity(identityId);

        if (identityKey == null) {
            KeyPair keyPair = signer.generateKeys();
            String publKey = keyPair.getPublKey();

            identityKey = new IdentityKey();
            identityKey.setIdentityId(identityId);
            identityKey.setPubKey(publKey);
            identityKey.setPrivKey(keyPair.getPrivKey());

            identityKeyDao.create(identityKey);
        }

        WebhookRecord record = getDslContext().newRecord(WEBHOOK, webHookModel);
        Query query = getDslContext()
                .insertInto(WEBHOOK)
                .set(record)
                .onConflict(WEBHOOK.ID)
                .doNothing()
                .returning(
                        WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID
                );

        Webhook webhook = fetchOne(query, webhookRowMapper);

        webHookModel.getEventTypes().forEach(
                eventType -> webHookToEventsDao.create(new WebhookToEvents(webhook.getId(), eventType))
        );

        log.info("webhook has been created, webHookModel={} ", webHookModel.toString());

        return webhook;
    }

    @Override
    public void delete(long id) {
        Query query = getDslContext().delete(WEBHOOK_TO_EVENTS).where(WEBHOOK_TO_EVENTS.HOOK_ID.eq(id));
        execute(query);

        query = getDslContext().delete(WEBHOOK).where(WEBHOOK.ID.eq(id));
        execute(query);

        log.info("webhook has been deleted, id={} ", id);
    }

    @Override
    public WebHookModel getById(long id) {
        Query query = getDslContext()
                .select(
                        WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID,
                        IDENTITY_KEY.PUB_KEY,
                        IDENTITY_KEY.PRIV_KEY
                )
                .from(WEBHOOK)
                .leftJoin(IDENTITY_KEY).on(WEBHOOK.IDENTITY_ID.eq(IDENTITY_KEY.IDENTITY_ID))
                .where(WEBHOOK.ID.eq(id));

        WebHookModel webHookModel = fetchOne(query, webHookModelRowMapper);

        if (webHookModel != null) {
            webHookModel.setEventTypes(
                    webHookToEventsDao.get(id).stream()
                            .map(WebhookToEvents::getEventType)
                            .collect(Collectors.toSet())
            );

            log.info("webhook has been got, webHookModel={} ", webHookModel.toString());
        }

        return webHookModel;
    }

    @Override
    public List<Webhook> getByIdentityAndWalletId(String identityId, String walletId, EventType eventType) {
        Query query = getDslContext()
                .select(
                        WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID
                )
                .from(WEBHOOK)
                .join(WEBHOOK_TO_EVENTS).on(WEBHOOK.ID.eq(WEBHOOK_TO_EVENTS.HOOK_ID))
                .where(
                        appendConditions(
                                DSL.trueCondition(),
                                Operator.AND,
                                new ConditionParameterSource()
                                        .addValue(WEBHOOK.IDENTITY_ID, identityId, EQUALS)
                                        .addValue(WEBHOOK.WALLET_ID, walletId, EQUALS)
                        )
                )
                .and(WEBHOOK_TO_EVENTS.EVENT_TYPE.eq(eventType))
                .limit(LIMIT);

        return getSafeWebHook(query, () -> log.info("webhooks has been got, identityId={}, walletId={}, eventType={} ", identityId, walletId, eventType));
    }

    @Override
    public List<WebHookModel> getModelByIdentityAndWalletId(String identityId, String walletId, EventType eventType) {
        Query query = getDslContext()
                .select(
                        WEBHOOK.ID,
                        WEBHOOK.IDENTITY_ID,
                        WEBHOOK.ENABLED,
                        WEBHOOK.URL,
                        WEBHOOK.WALLET_ID,
                        IDENTITY_KEY.PUB_KEY,
                        IDENTITY_KEY.PRIV_KEY
                )
                .from(WEBHOOK)
                .leftJoin(WEBHOOK_TO_EVENTS).on(WEBHOOK.ID.eq(WEBHOOK_TO_EVENTS.HOOK_ID))
                .leftJoin(IDENTITY_KEY).on(WEBHOOK.IDENTITY_ID.eq(IDENTITY_KEY.IDENTITY_ID))
                .where(
                        appendConditions(
                                DSL.trueCondition(),
                                Operator.AND,
                                new ConditionParameterSource()
                                        .addValue(WEBHOOK.IDENTITY_ID, identityId, EQUALS)
                                        .addValue(WEBHOOK.WALLET_ID, walletId, EQUALS)
                        )
                )
                .and(WEBHOOK_TO_EVENTS.EVENT_TYPE.eq(eventType))
                .limit(LIMIT);

        List<WebHookModel> webHookModels = fetch(query, webHookModelRowMapper);
        List<WebHookModel> webHookModelsSafe = webHookModels == null ? Collections.emptyList() : webHookModels;

        if (!webHookModelsSafe.isEmpty()) {
            webHookModels.forEach(
                    webHookModel -> webHookModel.setEventTypes(
                            webHookToEventsDao.get(webHookModel.getId()).stream()
                                    .map(WebhookToEvents::getEventType)
                                    .collect(Collectors.toSet())
                    )
            );

            log.info("webhooks has been got, webHookModels={}", getLogWebHookModel(webHookModelsSafe));
        }

        return webHookModelsSafe;
    }

    @Override
    public List<Webhook> getByIdentity(String identityId) {
        Query query = getDslContext()
                .selectFrom(WEBHOOK)
                .where(WEBHOOK.IDENTITY_ID.eq(identityId))
                .limit(LIMIT);

        return getSafeWebHook(query, () -> log.info("webhooks has been got, identityId={}", identityId));
    }

    private List<Webhook> getSafeWebHook(Query query, Runnable runIfNotEmpty) {
        List<Webhook> webhooks = fetch(query, webhookRowMapper);
        List<Webhook> webhooksSafe = webhooks == null ? Collections.emptyList() : webhooks;

        if (!webhooksSafe.isEmpty()) {
            runIfNotEmpty.run();
        }

        return webhooksSafe;
    }
}
