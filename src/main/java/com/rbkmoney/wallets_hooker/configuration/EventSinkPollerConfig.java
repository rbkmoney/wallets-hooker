package com.rbkmoney.wallets_hooker.configuration;

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
    public EventPublisher walletEventPublisher(
            WalletEventSinkHandler walletEventSinkHandler,
            @Value("${wallet.polling.url}") Resource resource,
            @Value("${wallet.polling.delay}") int pollDelay,
            @Value("${wallet.polling.retryDelay}") int retryDelay,
            @Value("${wallet.polling.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new FistfulPollingEventPublisherBuilder()
                .withWalletServiceAdapter()
                .withURI(resource.getURI())
                .withEventHandler(walletEventSinkHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }

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

    @Bean
    public EventPublisher destinationEventPublisher(
            DestinationEventSinkHandler destinationEventSinkHandler,
            @Value("${destination.polling.url}") Resource resource,
            @Value("${destination.polling.delay}") int pollDelay,
            @Value("${destination.polling.retryDelay}") int retryDelay,
            @Value("${destination.polling.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new FistfulPollingEventPublisherBuilder()
                .withDestinationServiceAdapter()
                .withURI(resource.getURI())
                .withEventHandler(destinationEventSinkHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }

}
