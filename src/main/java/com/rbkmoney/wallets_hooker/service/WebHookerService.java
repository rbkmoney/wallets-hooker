package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.fistful.webhooker.Webhook;
import com.rbkmoney.fistful.webhooker.WebhookManagerSrv;
import com.rbkmoney.fistful.webhooker.WebhookNotFound;
import com.rbkmoney.fistful.webhooker.WebhookParams;
import com.rbkmoney.wallets_hooker.converter.WebHookConverter;
import com.rbkmoney.wallets_hooker.converter.WebHookModelToWebHookConverter;
import com.rbkmoney.wallets_hooker.converter.WebHookParamsToWebHookConverter;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookerService implements WebhookManagerSrv.Iface {

    private final WebHookDao webHookDao;
    private final WebHookConverter webHookConverter;
    private final WebHookParamsToWebHookConverter webHookParamsToWebHookConverter;
    private final WebHookModelToWebHookConverter webHookModelToWebHookConverter;

    @Override
    public List<Webhook> getList(String identityId) {
        log.info("Start get webhooks, identityId={}", identityId);

        List<Webhook> webhooks = webHookDao.getByIdentity(identityId).stream()
                .peek(webhook -> log.info("Trying to convert webhook, webhook={}", webhook.toString()))
                .map(webHookConverter::convert)
                .peek(webhookDamsel -> log.info("webhook has been converted, webhookDamsel={}", webhookDamsel))
                .collect(Collectors.toList());

        log.info("Finish get webhooks, identityId={}, size={}", identityId, webhooks.size());

        return webhooks;
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound {
        log.info("Start get Webhook, id={}", id);

        WebHookModel webHookModel = webHookDao.getById(id);

        if (webHookModel == null) {
            log.warn("Webhook not found, {}", id);
            throw new WebhookNotFound();
        }

        log.info("webHookModel has been got, webHookModel={}", webHookModel.toString());
        log.info("Trying to convert webHookModel, webHookModel={}", webHookModel.toString());

        Webhook webhook = webHookModelToWebHookConverter.convert(webHookModel);

        log.info("webHookModel has been converted, webhook={}", webhook);
        log.info("Finish get Webhook, webhook={}", webhook);

        return webhook;
    }

    @Override
    public Webhook create(WebhookParams webhookParams) {
        try {
            log.info("Start create webhook, webhookParams={}", webhookParams);
            log.info("Trying to convert webhookParams, webhookParams={}", webhookParams);

            WebHookModel webHookModel = webHookParamsToWebHookConverter.convert(webhookParams);

            log.info("webhookParams has been converted, webHookModel={}", webHookModel.toString());
            log.info("Trying to create webhook, webHookModel={}", webHookModel.toString());

            var webhook = webHookDao.create(webHookModel);

            log.info("webhook has been created, webhook={}", webhook.toString());
            log.info("Trying to convert webhook, webhook={}", webhook);

            Webhook webhookResult = webHookConverter.convert(webhook);

            log.info("webhook has been converted, webhookResult={}", webhookResult);
            log.info("Finish create webhook, webhook={}", webhook);

            return webhookResult;
        } catch (Exception e) {
            log.error("Error when create webhook, {} ", webhookParams, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(long id) throws WebhookNotFound {
        try {
            log.info("Start delete webhook, id={}", id);

            webHookDao.delete(id);

            log.info("Finish delete webhook, id={}", id);
        } catch (Exception e) {
            log.error("Fail to delete webhook, {}", id, e);
            throw new WebhookNotFound();
        }
    }
}
