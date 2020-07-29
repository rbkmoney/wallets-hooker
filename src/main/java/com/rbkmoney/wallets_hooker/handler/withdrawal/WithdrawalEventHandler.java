package com.rbkmoney.wallets_hooker.handler.withdrawal;

import com.rbkmoney.fistful.withdrawal.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.handler.Handler;

public interface WithdrawalEventHandler extends Handler<TimestampedChange, MachineEvent> {
}
