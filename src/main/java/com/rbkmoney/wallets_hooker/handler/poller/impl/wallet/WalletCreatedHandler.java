package com.rbkmoney.wallets_hooker.handler.poller.impl.wallet;

import com.rbkmoney.fistful.wallet.Change;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WalletCreatedHandler extends AbstractWalletEventHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final WalletMessageDao messageDao;

    private Filter filter;

    public WalletCreatedHandler(WalletMessageDao messageDao) {
        this.messageDao = messageDao;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        WalletMessage message = new WalletMessage();
        message.setEventType(EventType.WALLET_CREATED);
        message.setEventId(event.getId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setWalletId(event.getSource());
        log.info("Start handling wallet created, walletId={}", event.getSource());
        Long messageId = messageDao.create(message);
        log.info("Finish handling wallet created, walletId={}, messageId={} saved to db.", event.getSource(), messageId);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
