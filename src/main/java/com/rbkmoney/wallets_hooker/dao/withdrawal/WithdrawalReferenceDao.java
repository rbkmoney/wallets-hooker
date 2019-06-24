package com.rbkmoney.wallets_hooker.dao.withdrawal;

import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;

public interface WithdrawalReferenceDao {

    void create(WithdrawalIdentityWalletReference reference);

    WithdrawalIdentityWalletReference get(String id);

}
