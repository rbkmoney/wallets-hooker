package com.rbkmoney.wallets_hooker.handler.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.TimestampedChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalSucceededHandler implements WithdrawalEventHandler {

    private final WithdrawalChangeStatusHandler withdrawalChangeStatusHandler;

    @SuppressWarnings("rawtypes")
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "status_changed.status.succeeded",
            new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange change, MachineEvent event) {
        String withdrawalId = event.getSourceId();
        log.info("Start handling WithdrawalSucceededChange: withdrawalId={} change={}", withdrawalId, change);

        withdrawalChangeStatusHandler.handleChangeStatus(
                change,
                event,
                withdrawalId,
                EventType.WITHDRAWAL_SUCCEEDED);

        log.info("Finish handling WithdrawalSucceededChange: withdrawalId={}", withdrawalId);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Filter getFilter() {
        return filter;
    }
}
