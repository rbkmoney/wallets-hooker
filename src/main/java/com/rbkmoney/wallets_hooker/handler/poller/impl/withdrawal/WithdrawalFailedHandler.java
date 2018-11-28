package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.swag_wallets_webhook_events.WithdrawalStatus;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalQueueDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalTaskDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalFailedHandler extends AbstractWithdrawalEventHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Filter filter;

    private final WithdrawalMessageDao withdrawalMessageDao;
    private final WithdrawalQueueDao queueDao;
    private final WithdrawalTaskDao taskDao;

    public WithdrawalFailedHandler(WithdrawalMessageDao withdrawalMessageDao, WithdrawalQueueDao queueDao, WithdrawalTaskDao taskDao) {
        this.withdrawalMessageDao = withdrawalMessageDao;
        this.queueDao = queueDao;
        this.taskDao = taskDao;
        filter = new PathConditionFilter(new PathConditionRule("status_changed.failed", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        String withdrawalId = event.getSource();
        WithdrawalMessage message = withdrawalMessageDao.getAny(withdrawalId);
        message.setEventType(EventType.WITHDRAWAL_FAILED);
        message.setEventId(event.getId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setWithdrawalStatus(WithdrawalStatus.StatusEnum.FAILED.name());
        message.setWithdrawalFailureCode(change.getStatusChanged().getFailed().getFailure().toString()); //TODO
        log.info("Start handling withdrawal failed, withdrawalId={}", event.getSource());
        Long messageId = withdrawalMessageDao.create(message);
        log.info("Finish handling withdrawal failed, withdrawalId={}, messageId={} saved to db.", event.getSource(), messageId);
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
