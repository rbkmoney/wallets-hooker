package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.wallets_hooker.handler.Handler;

public abstract class AbstractWithdrawalEventHandler implements Handler<Change, SinkEvent> {
}
