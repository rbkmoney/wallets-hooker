package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.swag_wallets_webhook_events.WithdrawalStatus;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalQueueDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalTaskDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalCreatedHandler extends AbstractWithdrawalEventHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final WithdrawalMessageDao withdrawalMessageDao;
    private final WalletMessageDao walletMessageDao;
    private final WithdrawalQueueDao queueDao;
    private final WithdrawalTaskDao taskDao;

    private Filter filter;

    public WithdrawalCreatedHandler(WithdrawalMessageDao withdrawalMessageDao, WalletMessageDao walletMessageDao, WithdrawalQueueDao queueDao, WithdrawalTaskDao taskDao) {
        this.withdrawalMessageDao = withdrawalMessageDao;
        this.walletMessageDao = walletMessageDao;
        this.queueDao = queueDao;
        this.taskDao = taskDao;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        Withdrawal withdrawal = change.getCreated();
        WithdrawalMessage message = new WithdrawalMessage();
        message.setEventType(EventType.WITHDRAWAL_CREATED);
        message.setEventId(event.getId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setPartyId(walletMessageDao.getAny(withdrawal.getSource()).getPartyId());
        message.setWithdrawalId(event.getSource());
        message.setWithdrawalCreatedAt(event.getCreatedAt());
        message.setWithdrawalWalletId(withdrawal.getSource());
        message.setWithdrawalDestinationId(withdrawal.getDestination());
        message.setWithdrawalAmount(withdrawal.getBody().getAmount());
        message.setWithdrawalCurrencyCode(withdrawal.getBody().getCurrency().getSymbolicCode());
        message.setWithdrawalStatus(WithdrawalStatus.StatusEnum.PENDING.name());
        log.info("Start handling withdrawal created, withdrawalId={}", event.getSource());
        Long messageId = withdrawalMessageDao.create(message);
        log.info("Finish handling withdrawal created, withdrawalId={}, messageId={} saved to db.", event.getSource(), messageId);
        int queueCount = queueDao.createByMessageId(messageId);
        log.info("Created {} queues for messageId {}", queueCount, messageId);
        int taskCount = taskDao.createByMessageId(messageId);
        log.info("Created tasks count {} for messageId {}", taskCount, messageId);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
