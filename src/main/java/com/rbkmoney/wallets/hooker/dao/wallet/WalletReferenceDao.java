package com.rbkmoney.wallets.hooker.dao.wallet;

import com.rbkmoney.wallets.hooker.domain.tables.pojos.WalletIdentityReference;

public interface WalletReferenceDao {

    void create(WalletIdentityReference reference);

    WalletIdentityReference get(String id);

}
