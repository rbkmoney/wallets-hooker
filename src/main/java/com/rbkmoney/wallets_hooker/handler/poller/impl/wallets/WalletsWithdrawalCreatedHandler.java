package com.rbkmoney.wallets_hooker.handler.poller.impl.wallets;

import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WalletsWithdrawalCreatedHandler extends AbstractInvoiceEventHandler {

    @Autowired
    private WalletsMessageDao messageDao;

    private Filter filter;

    private EventType eventType = EventType.WALLET_WITHDRAWAL_CREATED;

    public WalletsWithdrawalCreatedHandler() {
        filter = new PathConditionFilter(new PathConditionRule(eventType.getThriftFilterPathCoditionRule(), new IsNullCondition().not()));
    }

    @Override
    @Transactional
    protected void saveEvent(InvoiceChange ic, Event event) throws DaoException {
        Invoice invoiceOrigin = ic.getInvoiceCreated().getInvoice();
        WalletsMessage message = ConverterUtils.buildWalletsMessage(
                eventType.name(),
                invoiceOrigin.getOwnerId(),
                invoiceOrigin.getId(),
                event.getCreatedAt(),
                event.getId());
        messageDao.create(message);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
