package com.rbkmoney.wallets_hooker.dao.destination;

import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;

public interface DestinationReferenceDao {

    void create(DestinationIdentityReference reference);

    DestinationIdentityReference get(String id);

}
