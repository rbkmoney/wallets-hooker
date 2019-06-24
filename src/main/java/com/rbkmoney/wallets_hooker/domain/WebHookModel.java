package com.rbkmoney.wallets_hooker.domain;

import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class WebHookModel {

    private Long id;
    private String identityId;
    private String walletId;
    private Set<EventType> eventTypes;
    private String url;
    private Boolean enabled;
    private String pubKey;

}
