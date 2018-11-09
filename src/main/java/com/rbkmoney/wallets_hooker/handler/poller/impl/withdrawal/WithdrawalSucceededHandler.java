package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
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
public class WithdrawalSucceededHandler extends AbstractWithdrawalEventHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Filter filter;

    private final WithdrawalMessageDao withdrawalMessageDao;
    private final WithdrawalQueueDao queueDao;
    private final WithdrawalTaskDao taskDao;

    public WithdrawalSucceededHandler(WithdrawalMessageDao withdrawalMessageDao, WithdrawalQueueDao queueDao, WithdrawalTaskDao taskDao) {
        this.withdrawalMessageDao = withdrawalMessageDao;
        this.queueDao = queueDao;
        this.taskDao = taskDao;
        filter = new PathConditionFilter(new PathConditionRule("status_changed.succeeded", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        String withdrawalId = event.getSource();
        WithdrawalMessage message = withdrawalMessageDao.getAny(withdrawalId);
        message.setEventType(EventType.WITHDRAWAL_SUCCEEDED);
        message.setEventId(event.getId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setWithdrawalStatus(WithdrawalStatus.StatusEnum.SUCCEEDED.name());
        log.info("Start handling withdrawal succeeded, withdrawalId={}", event.getSource());
        Long messageId = withdrawalMessageDao.create(message);
        log.info("Finish handling withdrawal succeeded, withdrawalId={}, messageId={} saved to db.", event.getSource(), messageId);
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
