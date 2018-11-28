package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;

public interface WithdrawalMessageDao extends MessageDao<WithdrawalMessage> {
    WithdrawalMessage getAny(String withdrawalId) throws DaoException;
}
