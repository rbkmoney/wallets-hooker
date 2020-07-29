package com.rbkmoney.wallets_hooker.handler.wallet;

import com.rbkmoney.fistful.wallet.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.handler.Handler;

public interface WalletEventHandler extends Handler<TimestampedChange, MachineEvent> {
}
