package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.WalletMessage;

public interface WalletMessageDao extends MessageDao<WalletMessage> {
    WalletMessage getAny(String walletId) throws DaoException;
}
