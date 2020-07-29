package com.rbkmoney.wallets_hooker.kafka.listener;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationEventListener {

    private final DestinationEventService destinationEventService;

    @KafkaListener(
            autoStartup = "${kafka.topic.destination.listener.enabled}",
            topics = "${kafka.topic.destination.name}",
            containerFactory = "destinationEventListenerContainerFactory")
    public void listen(
            List<SinkEvent> batch,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) int offset,
            Acknowledgment ack) {
        log.info("Listening Destination: partition={}, offset={}, batch.size()={}", partition, offset, batch.size());
        destinationEventService.handleEvents(batch.stream().map(SinkEvent::getEvent).collect(toList()));
        ack.acknowledge();
        log.info("Ack Destination: partition={}, offset={}", partition, offset);
    }
}