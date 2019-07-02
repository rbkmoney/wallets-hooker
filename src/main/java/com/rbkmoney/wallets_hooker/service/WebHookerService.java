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
    public List<Webhook> getList(String id) {
        return webHookDao.getByIdentity(id).stream()
                .map(webHookConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound {
        WebHookModel webHookModel = webHookDao.getById(id);
        if (webHookModel == null) {
            log.warn("Webhook not found: {}", id);
            throw new WebhookNotFound();
        }
        return webHookModelToWebHookConverter.convert(webHookModel);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) {
        com.rbkmoney.wallets_hooker.domain.tables.pojos.Webhook webhook = webHookDao.create(webHookParamsToWebHookConverter.convert(webhookParams));
        log.info("Webhook webhookParams: {}", webhookParams);
        return webHookConverter.convert(webhook);
    }

    @Override
    public void delete(long id) throws WebhookNotFound {
        try {
            webHookDao.delete(id);
            log.info("Webhook deleted: {}", id);
        } catch (Exception e){
            log.error("Fail to delete webhook: {}", id, e);
            throw new WebhookNotFound();
        }
    }
}
