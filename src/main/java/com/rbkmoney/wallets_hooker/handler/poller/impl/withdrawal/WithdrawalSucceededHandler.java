package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalSucceededHandler extends AbstractWithdrawalEventHandler {

    private final WithdrawalChangeStatusHandler withdrawalChangeStatusHandler;
    private Filter filter = new PathConditionFilter(new PathConditionRule("status_changed.succeeded", new IsNullCondition().not()));

    @Override
    public void handle(Change change, SinkEvent event) {
        log.info("Handle withdrawal succeeded: {} ", change.getStatusChanged());
        String withdrawalId = event.getSource();
        withdrawalChangeStatusHandler.handleChangeStatus(change, event, withdrawalId, EventType.WITHDRAWAL_SUCCEEDED);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
