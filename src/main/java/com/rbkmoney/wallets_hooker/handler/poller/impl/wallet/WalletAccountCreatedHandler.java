package com.rbkmoney.wallets_hooker.handler.poller.impl.wallet;

import com.rbkmoney.fistful.wallet.Change;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.IdentityMessageDao;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WalletAccountCreatedHandler extends AbstractWalletEventHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final WalletMessageDao walletMessageDao;
    private final IdentityMessageDao identityMessageDao;

    private Filter filter;

    public WalletAccountCreatedHandler(WalletMessageDao walletMessageDao, IdentityMessageDao identityMessageDao) {
        this.walletMessageDao = walletMessageDao;
        this.identityMessageDao = identityMessageDao;
        filter = new PathConditionFilter(new PathConditionRule("account.created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        String walletId = event.getSource();
        String identityId = change.getAccount().getCreated().getIdentity();
        WalletMessage message = walletMessageDao.getAny(walletId);
        message.setEventType(EventType.WALLET_ACCOUNT_CREATED);
        message.setEventId(event.getId());
        message.setPartyId(identityMessageDao.getAny(identityId).getPartyId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setIdentityId(identityId);
        log.info("Start handling wallet account created, walletId={}", event.getSource());
        Long messageId = walletMessageDao.create(message);
        log.info("Finish handling wallet account created, walletId={}, messageId={} saved to db.", event.getSource(), messageId);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
