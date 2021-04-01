package com.rbkmoney.wallets.hooker.dao.withdrawal;

import com.rbkmoney.wallets.hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;

public interface WithdrawalReferenceDao {

    void create(WithdrawalIdentityWalletReference reference);

    WithdrawalIdentityWalletReference get(String id);

}
