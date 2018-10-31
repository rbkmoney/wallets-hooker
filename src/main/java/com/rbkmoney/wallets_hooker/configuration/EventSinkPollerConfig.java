package com.rbkmoney.wallets_hooker.configuration;

import com.rbkmoney.eventstock.client.*;
import com.rbkmoney.eventstock.client.poll.*;
import com.rbkmoney.wallets_hooker.handler.poller.IdentityEventSinkHandler;
import com.rbkmoney.wallets_hooker.handler.poller.WalletEventSinkHandler;
import com.rbkmoney.wallets_hooker.handler.poller.WithdrawalEventSinkHandler;
import com.rbkmoney.woody.api.ClientBuilder;
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
        return new PollingEventPublisherBuilder() {
            @Override
            protected ServiceAdapter createServiceAdapter(ClientBuilder clientBuilder) {
                return FistfulServiceAdapter.buildWalletAdapter(clientBuilder);
            }
        }
                .withURI(resource.getURI())
                .withEventHandler(walletEventSinkHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }

    @Bean
    public EventPublisher identityEventPublisher(
            IdentityEventSinkHandler identityEventSinkHandler,
            @Value("${identity.polling.url}") Resource resource,
            @Value("${identity.polling.delay}") int pollDelay,
            @Value("${identity.polling.retryDelay}") int retryDelay,
            @Value("${identity.polling.maxPoolSize}") int maxPoolSize
    ) throws IOException {
        return new PollingEventPublisherBuilder() {
            @Override
            protected ServiceAdapter createServiceAdapter(ClientBuilder clientBuilder) {
                return FistfulServiceAdapter.buildIdentityAdapter(clientBuilder);
            }
        }
                .withURI(resource.getURI())
                .withEventHandler(identityEventSinkHandler)
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
        return new PollingEventPublisherBuilder() {
            @Override
            protected ServiceAdapter createServiceAdapter(ClientBuilder clientBuilder) {
                return FistfulServiceAdapter.buildWithdrawalAdapter(clientBuilder);
            }
        }
                .withURI(resource.getURI())
                .withEventHandler(withdrawalEventSinkHandler)
                .withMaxPoolSize(maxPoolSize)
                .withEventRetryDelay(retryDelay)
                .withPollDelay(pollDelay)
                .build();
    }

}
