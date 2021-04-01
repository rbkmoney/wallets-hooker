package com.rbkmoney.wallets.hooker.dao.destination;

import com.rbkmoney.wallets.hooker.domain.tables.pojos.DestinationMessage;

public interface DestinationMessageDao {

    void create(DestinationMessage reference);

    DestinationMessage get(String id);

}
