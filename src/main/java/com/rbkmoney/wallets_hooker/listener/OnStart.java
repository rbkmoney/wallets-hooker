package com.rbkmoney.wallets_hooker.listener;

import com.rbkmoney.eventstock.client.DefaultSubscriberConfig;
import com.rbkmoney.eventstock.client.EventConstraint;
import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.SubscriberConfig;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.wallets_hooker.constant.EventTopic;
import com.rbkmoney.wallets_hooker.dao.EventLogDao;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OnStart implements ApplicationListener<ApplicationReadyEvent> {

    private final EventPublisher destinationEventPublisher;
    private final EventPublisher withdrawalEventPublisher;
    private final EventPublisher walletEventPublisher;

    private final EventLogDao eventLogDao;

    @Value("${withdrawal.polling.lastEventId}")
    private Long withdrawalLastEventId;

    @Value("${destination.polling.lastEventId}")
    private Long destinationLastEventId;

    @Value("${wallet.polling.lastEventId}")
    private Long walletLastEventId;

    @Value("${fistful.pollingEnabled}")
    private boolean pollingEnabled;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (pollingEnabled) {
            destinationEventPublisher.subscribe(buildSubscriberConfig(
                    Optional.ofNullable(eventLogDao.getLastEventId(EventTopic.DESTINATION, destinationLastEventId))));
            walletEventPublisher.subscribe(buildSubscriberConfig(
                    Optional.ofNullable(eventLogDao.getLastEventId(EventTopic.WALLET, walletLastEventId))));
            withdrawalEventPublisher.subscribe(buildSubscriberConfig(
                    Optional.ofNullable(eventLogDao.getLastEventId(EventTopic.WITHDRAWAL, withdrawalLastEventId))));
        }
    }

    private SubscriberConfig buildSubscriberConfig(Optional<Long> lastEventIdOptional) {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        lastEventIdOptional.ifPresent(eventIDRange::setFromExclusive);
        EventFlowFilter eventFlowFilter = new EventFlowFilter(new EventConstraint(eventIDRange));
        return new DefaultSubscriberConfig(eventFlowFilter);
    }
}