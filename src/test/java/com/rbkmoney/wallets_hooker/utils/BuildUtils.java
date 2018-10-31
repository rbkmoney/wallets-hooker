package com.rbkmoney.wallets_hooker.utils;

import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;

import static io.github.benas.randombeans.api.EnhancedRandom.random;

public class BuildUtils {
    public static WithdrawalMessage buildWithdrawalMessage(EventType eventType, String partyId, String withdrawalId) {
        WithdrawalMessage message = random(WithdrawalMessage.class);
        message.setEventType(eventType);
        message.setPartyId(partyId);
        message.setWithdrawalId(withdrawalId);
        return message;
    }
}
