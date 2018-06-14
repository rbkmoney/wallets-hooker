package com.rbkmoney.wallets_hooker.handler.poller.impl.wallets;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletWithdrawalFailedHandler extends NeedReadWalletWithdrawalEventHandler {

    private EventType eventType = EventType.WALLET_WITHDRAWAL_SUCCEEDED;
    private Filter filter;

    @Autowired
    private WalletsMessageDao messageDao;

    public WalletWithdrawalFailedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    protected WalletsMessage getMessage(String walletId, InvoiceChange ic) {
        return messageDao.getAny(walletId);
    }

    @Override
    protected EventType getEventType() {
        return eventType;
    }

    @Override
    protected void modifyMessage(InvoiceChange ic, Event event, WalletsMessage message) {
    }
}
