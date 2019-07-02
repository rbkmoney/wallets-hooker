package com.rbkmoney.wallets_hooker.dao.destination;

import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationMessage;

public interface DestinationMessageDao {

    void create(DestinationMessage reference);

    DestinationMessage get(String id);

}
