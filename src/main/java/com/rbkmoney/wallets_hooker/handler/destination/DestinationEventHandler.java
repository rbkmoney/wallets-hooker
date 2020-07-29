package com.rbkmoney.wallets_hooker.handler.destination;

import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.handler.Handler;

public interface DestinationEventHandler extends Handler<TimestampedChange, MachineEvent> {
}
