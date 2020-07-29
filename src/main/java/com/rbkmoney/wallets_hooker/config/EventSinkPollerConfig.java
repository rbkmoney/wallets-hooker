package com.rbkmoney.wallets_hooker.config;

import com.rbkmoney.eventstock.client.EventPublisher;
import com.rbkmoney.eventstock.client.poll.FistfulPollingEventPublisherBuilder;
import com.rbkmoney.wallets_hooker.handler.poller.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class EventSinkPollerConfig {

    @Bean
    public EventPublisher withdrawalEventPublisher(
            WithdrawalEventSinkHandler withdrawalEventSinkHandler,
            @Value("${withdrawal.polling.url}") Resource resource,
            @Value("${withdrawal.polling.delay}") int pollDelay,
            @Value("${withdrawal.polling.retryDelay}") int retryDelay,
            @Value("${withdrawal.polling.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new FistfulPollingEventPublisherBuilder()
                .withWithdrawalServiceAdapter()
                .withURI(resource.getURI())
                .withEventHandler(withdrawalEventSinkHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }
}
