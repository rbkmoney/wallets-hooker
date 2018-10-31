package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.swag_wallets_webhook_events.WithdrawalStatus;
import com.rbkmoney.wallets_hooker.dao.DaoException;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalFailedHandler extends AbstractWithdrawalEventHandler {
    private Filter filter;

    private final WithdrawalMessageDao withdrawalMessageDao;
    private final WalletMessageDao walletMessageDao;

    public WithdrawalFailedHandler(WithdrawalMessageDao withdrawalMessageDao, WalletMessageDao walletMessageDao) {
        this.withdrawalMessageDao = withdrawalMessageDao;
        this.walletMessageDao = walletMessageDao;
        filter = new PathConditionFilter(new PathConditionRule("status_changed.failed", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        String withdrawalId = event.getSource();
        WithdrawalMessage message = withdrawalMessageDao.getAny(withdrawalId);
        message.setEventType(EventType.WITHDRAWAL_FAILED);
        message.setEventId(event.getPayload().getId());
        message.setPartyId(walletMessageDao.getAny(event.getSource()).getPartyId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setWithdrawalStatus(WithdrawalStatus.StatusEnum.FAILED.getValue()); //TODO Check
        message.setWithdrawalFailureCode(change.getStatusChanged().getFailed().getFailure().toString()); //TODO
        withdrawalMessageDao.create(message);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
