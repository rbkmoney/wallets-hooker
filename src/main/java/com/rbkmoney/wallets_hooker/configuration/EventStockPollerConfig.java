package com.rbkmoney.wallets_hooker.configuration;

import com.rbkmoney.eventstock.client.*;
import com.rbkmoney.eventstock.client.poll.EventFlowFilter;
import com.rbkmoney.eventstock.client.poll.PPServiceAdapter;
import com.rbkmoney.eventstock.client.poll.PollingEventPublisherBuilder;
import com.rbkmoney.eventstock.client.poll.ServiceAdapter;
import com.rbkmoney.wallets_hooker.handler.Handler;
import com.rbkmoney.wallets_hooker.handler.poller.EventStockHandler;
import com.rbkmoney.wallets_hooker.service.EventService;
import com.rbkmoney.woody.api.ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class EventStockPollerConfig {

    @Value("${hg.polling.url}")
    Resource hgUri;

    @Value("${hg.polling.delay}")
    int pollDelay;

    @Value("${hg.polling.maxPoolSize}")
    int maxPoolSize;

    @Autowired
    List<Handler> pollingEventHandlers;

    @Autowired
    EventService eventService;

    @Bean(destroyMethod = "destroy")
    public EventPublisher eventPublisher() throws IOException {
        PollingEventPublisherBuilder builder = new PollingEventPublisherBuilder() {
            @Override
            protected ServiceAdapter createServiceAdapter(ClientBuilder clientBuilder) {
                return PPServiceAdapter.build(clientBuilder);
            }
        };
        return builder
                .withURI(hgUri.getURI())
                .withEventHandler(new EventStockHandler(pollingEventHandlers))
                .withMaxPoolSize(maxPoolSize)
                .withPollDelay(pollDelay)
                .build();
    }

    @Bean
    public SubscriberConfig subscriberConfig() {
        return new DefaultSubscriberConfig(eventFilter());
    }

    public EventFilter eventFilter() {
        EventConstraint.EventIDRange eventIDRange = new EventConstraint.EventIDRange();
        Long lastEventId = eventService.getLastEventId();
        if (lastEventId != null) {
            eventIDRange.setFromExclusive(lastEventId);
        } else {
            eventIDRange.setFromNow();
        }
        return new EventFlowFilter(new EventConstraint(eventIDRange));
    }

}
