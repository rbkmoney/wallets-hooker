package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.WalletsMessage;

public interface WalletsMessageDao extends MessageDao<WalletsMessage> {
    WalletsMessage getAny(String walletId) throws DaoException;
}
