package com.rbkmoney.wallets_hooker.handler.wallet;

import com.rbkmoney.fistful.wallet.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.handler.EventHandler;

public interface WalletEventHandler extends EventHandler<TimestampedChange, MachineEvent> {
}
