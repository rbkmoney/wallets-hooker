package com.rbkmoney.wallets_hooker.listener;

import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OnStart implements ApplicationListener<ApplicationReadyEvent> {

//    private final EventPublisher identityEventPublisher;
//    private final EventPublisher withdrawalEventPublisher;
//    private final EventPublisher walletEventPublisher;
//
//    private final WalletMessageDao walletMessageDao;
//    private final IdentityMessageDao identityMessageDao;
//    private final WithdrawalMessageDao withdrawalMessageDao;
//
//    @Value("${withdrawal.polling.lastEventId}")
//    private Long withdrawalLastEventId;
//
//    @Value("${fistful.pollingEnabled}")
//    private boolean pollingEnabled;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
//        if (pollingEnabled) {
//            identityEventPublisher.subscribe(buildSubscriberConfig(Optional.ofNullable(identityMessageDao.getLastEventId())));
//            walletEventPublisher.subscribe(buildSubscriberConfig(Optional.ofNullable(walletMessageDao.getLastEventId())));
//            Long lastEventId = withdrawalMessageDao.getLastEventId();
//            if (lastEventId == null) {
//                lastEventId = withdrawalLastEventId;
//            }
//            withdrawalEventPublisher.subscribe(buildSubscriberConfig(Optional.ofNullable(lastEventId)));
//        }
    }

    private SubscriberConfig buildSubscriberConfig(Optional<Long> lastEventIdOptional) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        lastEventIdOptional.ifPresent(eventIDRange::setFromExclusive);
        EventFlowFilter eventFlowFilter = new EventFlowFilter(new EventConstraint(eventIDRange));
        return new DefaultSubscriberConfig(eventFlowFilter);
    }
}