package com.rbkmoney.wallets_hooker.dao.identity;

import com.rbkmoney.wallets_hooker.domain.tables.pojos.IdentityKey;

public interface IdentityKeyDao {

    void create(IdentityKey identityKey);

    IdentityKey get(Long id);

    IdentityKey getByIdentity(String id);

}
