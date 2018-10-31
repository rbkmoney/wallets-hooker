package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.fistful.webhooker.Webhook;
import com.rbkmoney.fistful.webhooker.WebhookManagerSrv;
import com.rbkmoney.fistful.webhooker.WebhookNotFound;
import com.rbkmoney.fistful.webhooker.WebhookParams;
import com.rbkmoney.wallets_hooker.dao.HookDao;
import com.rbkmoney.wallets_hooker.model.Hook;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by inalarsanukaev on 06.04.17.
 */
@Service
public class HookerService implements WebhookManagerSrv.Iface {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    HookDao hookDao;

    @Override
    public List<Webhook> getList(String s) throws TException {
        return ConverterUtils.convertHooks(hookDao.getPartyHooks(s));
    }

    @Override
    public Webhook get(long id) throws WebhookNotFound {
        Hook hook = hookDao.getHookById(id);
        if (hook == null) {
            log.warn("Webhook not found: {}", id);
            throw new WebhookNotFound();
        }
        return ConverterUtils.convertHook(hook);
    }

    @Override
    public Webhook create(WebhookParams webhookParams) throws TException {
        Hook hook = hookDao.create(ConverterUtils.convertHook(webhookParams));
        log.info("Webhook created: {}", hook);
        return ConverterUtils.convertHook(hook);
    }

    @Override
    public void delete(long id) throws WebhookNotFound{
        try {
            hookDao.delete(id);
            log.info("Webhook deleted: {}", id);
        } catch (Exception e){
            log.error("Fail to delete webhook: {}", id, e);
            throw new WebhookNotFound();
        }
    }
}
