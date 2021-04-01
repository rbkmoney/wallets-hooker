package com.rbkmoney.wallets.hooker.dao.identity;

import com.rbkmoney.wallets.hooker.domain.tables.pojos.IdentityKey;

public interface IdentityKeyDao {

    void create(IdentityKey identityKey);

    IdentityKey get(Long id);

    IdentityKey getByIdentity(String identityId);

}
