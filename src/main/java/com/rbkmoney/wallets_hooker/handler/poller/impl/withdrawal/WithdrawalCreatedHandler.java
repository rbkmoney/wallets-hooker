package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalCreatedHandler extends AbstractWithdrawalEventHandler {

    private final WithdrawalMessageDao withdrawalMessageDao;
    private final WalletMessageDao walletMessageDao;

    private Filter filter;

    public WithdrawalCreatedHandler(WithdrawalMessageDao withdrawalMessageDao, WalletMessageDao walletMessageDao) {
        this.withdrawalMessageDao = withdrawalMessageDao;
        this.walletMessageDao = walletMessageDao;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        Withdrawal withdrawal = change.getCreated();
        WithdrawalMessage message = new WithdrawalMessage();
        message.setEventType(EventType.WITHDRAWAL_CREATED);
        message.setEventId(event.getPayload().getId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setPartyId(walletMessageDao.getAny(event.getSource()).getPartyId());
        message.setWithdrawalId(event.getSource());
        message.setWithdrawalCreatedAt(event.getCreatedAt());
        message.setWithdrawalWalletId(withdrawal.getSource());
        message.setWithdrawalDestinationId(withdrawal.getDestination());
        message.setWithdrawalAmount(withdrawal.getBody().getAmount());
        message.setWithdrawalCurrencyCode(withdrawal.getBody().getCurrency().getSymbolicCode());
        withdrawalMessageDao.create(message);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
