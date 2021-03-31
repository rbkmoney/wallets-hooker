package com.rbkmoney.wallets.hooker.handler.destination;

import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets.hooker.handler.EventHandler;

public interface DestinationEventHandler extends EventHandler<TimestampedChange, MachineEvent> {
}
