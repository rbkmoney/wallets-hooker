package com.rbkmoney.wallets_hooker.handler.poller.impl.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageGenParams {

    private String sourceId;

    private Long eventId;

    private Long parentId;

    private String createdAt;

    private String externalId;

}
