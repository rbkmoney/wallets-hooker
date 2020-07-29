package com.rbkmoney.wallets_hooker.handler.withdrawal;

import com.rbkmoney.fistful.withdrawal.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.handler.EventHandler;

public interface WithdrawalEventHandler extends EventHandler<TimestampedChange, MachineEvent> {
}
