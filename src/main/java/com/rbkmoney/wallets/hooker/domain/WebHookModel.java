package com.rbkmoney.wallets.hooker.domain;

import com.rbkmoney.wallets.hooker.domain.enums.EventType;
import lombok.*;

import java.util.Set;

@Data
@ToString(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebHookModel {

    @ToString.Include
    private Long id;
    @ToString.Include
    private String identityId;
    @ToString.Include
    private String walletId;
    @ToString.Include
    private Set<EventType> eventTypes;
    @ToString.Include
    private String url;
    @ToString.Include
    private Boolean enabled;
    private String pubKey;
    private String privateKey;

}
