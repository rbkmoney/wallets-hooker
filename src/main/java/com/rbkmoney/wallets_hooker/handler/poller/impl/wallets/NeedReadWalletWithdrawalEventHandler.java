package com.rbkmoney.wallets_hooker.handler.poller.impl.wallets;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public abstract class NeedReadWalletWithdrawalEventHandler extends AbstractInvoiceEventHandler{
    @Autowired
    private WalletsMessageDao messageDao;

    @Override
    protected void saveEvent(InvoiceChange ic, Event event) throws DaoException {
        final String walletId = event.getSource().getInvoiceId();
        //getAny any saved message for related invoice
        WalletsMessage message = getMessage(walletId, ic);
        if (message == null) {
            throw new DaoException("WalletsMessage for wallet with id " + walletId + " not exist");
        }
        message.setEventType(getEventType());
        message.getEvent().setEventID((int) event.getId());
        message.getEvent().setOccuredAt(OffsetDateTime.ofInstant(TypeUtil.stringToInstant(event.getCreatedAt()), ZoneOffset.UTC));
        message.getEvent().setEventType(ConverterUtils.convertSwagEventType(getEventType()));
        modifyMessage(ic, event, message);
        messageDao.create(message);
    }

    protected abstract WalletsMessage getMessage(String invoiceId, InvoiceChange ic);

    protected abstract EventType getEventType();

    protected abstract void modifyMessage(InvoiceChange ic, Event event, WalletsMessage message);

}
