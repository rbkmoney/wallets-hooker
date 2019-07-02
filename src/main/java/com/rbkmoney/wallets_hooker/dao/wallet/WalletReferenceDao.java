package com.rbkmoney.wallets_hooker.dao.wallet;

import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;

public interface WalletReferenceDao {

    void create(WalletIdentityReference reference);

    WalletIdentityReference get(String id);

}
