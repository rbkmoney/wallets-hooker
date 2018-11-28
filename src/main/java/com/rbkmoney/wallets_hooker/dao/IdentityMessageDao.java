package com.rbkmoney.wallets_hooker.dao;

import com.rbkmoney.wallets_hooker.model.IdentityMessage;

public interface IdentityMessageDao extends MessageDao<IdentityMessage> {
    IdentityMessage getAny(String identityId) throws DaoException;
}
