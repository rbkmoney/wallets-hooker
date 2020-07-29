package com.rbkmoney.wallets_hooker.handler.withdrawal;

import com.rbkmoney.fistful.withdrawal.TimestampedChange;
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

    @Override
    public boolean accept(TimestampedChange change) {
        return change.getChange().isSetStatusChanged()
                && change.getChange().getStatusChanged().isSetStatus()
                && change.getChange().getStatusChanged().getStatus().isSetSucceeded();
    }

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
}
