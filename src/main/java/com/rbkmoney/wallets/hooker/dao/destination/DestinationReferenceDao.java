package com.rbkmoney.wallets.hooker.dao.destination;

import com.rbkmoney.wallets.hooker.domain.tables.pojos.DestinationIdentityReference;

public interface DestinationReferenceDao {

    void create(DestinationIdentityReference reference);

    DestinationIdentityReference get(String id);

}
